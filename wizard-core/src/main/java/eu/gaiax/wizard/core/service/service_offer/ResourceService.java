package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartsensesolutions.java.commons.FilterRequest;
import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.filter.FilterCriteria;
import com.smartsensesolutions.java.commons.operator.Operator;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ResourceFilterResponse;
import eu.gaiax.wizard.api.model.ResourceType;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.resource.ResourceRepository;
import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceService extends BaseService<Resource, UUID> {

    private final ResourceRepository repository;

    private final ParticipantService participantService;

    private final CertificateService certificateService;

    private final ParticipantRepository participantRepository;

    private final ContextConfig contextConfig;

    private final ObjectMapper objectMapper;

    private final S3Utils s3Utils;

    private final CredentialService credentialService;

    private final SpecificationUtil<Resource> specificationUtil;

    private final SignerService signerService;

    private final Vault vault;

    @Value("${wizard.host.wizard}")
    private String wizardHost;

    @NotNull
    private static List<Map<String, Object>> getMaps(Participant participant) {
        List<Map<String, Object>> permission = new ArrayList<>();
        Map<String, Object> perMap = new HashMap<>();
        perMap.put("target", participant.getDid());
        perMap.put("assigner", participant.getDid());
        perMap.put("action", "view");
        List<Map<String, Object>> constraint = new ArrayList<>();
        Map<String, Object> constraintMap = new HashMap<>();
        constraintMap.put("default", "allow");
        constraint.add(constraintMap);
        perMap.put("constraint", constraint);
        permission.add(perMap);
        return permission;
    }

    @SneakyThrows
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Resource createResource(CreateResourceRequest request, String id) throws JsonProcessingException {
        Participant participant;
        if (StringUtils.hasText(id)) {
            participant = this.participantRepository.findById(UUID.fromString(id)).orElse(null);
            Validate.isNull(participant).launch(new BadDataException("participant.not.found"));

            if (participant.isKeyStored()) {
                if (!this.vault.get(participant.getId().toString()).containsKey("pkcs8.key")) {
                    throw new BadDataException("private.key.not.found");
                }

                request.setPrivateKey(this.vault.get(participant.getId().toString()).get("pkcs8.key").toString());
                request.setVerificationMethod(participant.getDid());
            }
            if (request.isStoreVault() && !participant.isKeyStored()) {
                this.certificateService.uploadCertificatesToVault(participant.getId().toString(), null, null, null, request.getPrivateKey());
                participant.setKeyStored(true);
                this.participantRepository.save(participant);
            }
            Credential participantCred = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
            this.signerService.validateRequestUrl(Collections.singletonList(participantCred.getVcUrl()), "participant.json.not.found", null);
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJsonUrl(), request.getVerificationMethod(), request.getPrivateKey(), false);
            participant = this.participantService.validateParticipant(participantValidatorRequest);
        }

        Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        this.validateResourceRequest(request);
        String name = "resource_" + UUID.randomUUID();
        String json = this.resourceVc(request, participant, name);
        String hostUrl = participant.getId() + "/" + name + ".json";

        if (StringUtils.hasText(json)) {
            Credential resourceVc = this.credentialService.createCredential(json, this.wizardHost + hostUrl, CredentialTypeEnum.RESOURCE.getCredentialType(), "", participant);
            Resource resource = Resource.builder().name(request.getCredentialSubject().get("gx:name").toString())
                    .credential(resourceVc)
                    .type(request.getCredentialSubject().get("type").toString())
                    .description((String) request.getCredentialSubject().getOrDefault("gx:description", null))
                    .participant(participant)
                    .build();

            if (resource.getType().equals(ResourceType.VIRTUAL_DATA_RESOURCE.getValue())) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                if (request.getCredentialSubject().containsKey("gx:obsoleteDateTime")) {
                    resource.setObsoleteDate(formatter.parse((String) request.getCredentialSubject().get("gx:obsoleteDateTime")));
                }

                if (request.getCredentialSubject().containsKey("gx:expirationDateTime")) {
                    resource.setExpiryDate(formatter.parse((String) request.getCredentialSubject().get("gx:expirationDateTime")));
                }
            }

            return this.repository.save(resource);
        }

        if (StringUtils.hasText(id) && request.isStoreVault() && !participant.isKeyStored()) {
            this.certificateService.uploadCertificatesToVault(participant.getId().toString(), null, null, null, request.getPrivateKey());
            participant.setKeyStored(true);
            this.participantRepository.save(participant);
        }
        return null;
    }

    private String hostOdrlPolicy(Participant participant) throws JsonProcessingException {
        Map<String, Object> policyMap = new HashMap<>();

        String hostUrl = participant.getId() + "/resource_policy_" + UUID.randomUUID() + ".json";
        policyMap.put("@context", this.contextConfig.ODRLPolicy());
        policyMap.put("type", "Offer");
        policyMap.put("id", this.wizardHost + hostUrl);
        List<Map<String, Object>> permission = getMaps(participant);
        policyMap.put("permission", permission);
        String policyJson = this.objectMapper.writeValueAsString(policyMap);
        File file = new File("/tmp/" + hostUrl);
        try {
            FileUtils.writeStringToFile(file, policyJson, Charset.defaultCharset());
            this.s3Utils.uploadFile(hostUrl, file);
            return this.wizardHost + hostUrl;
        } catch (Exception e) {
            log.error("Error while hosting service offer json for participant:{},error:{}", hostUrl, e.getMessage());
            throw new BadDataException(e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }

    private void validateResourceRequest(CreateResourceRequest request) throws JsonProcessingException {
        Validate.isFalse(StringUtils.hasText(request.getCredentialSubject().get("gx:name").toString())).launch("invalid.resource.name");
        this.validateAggregationOf(request);
    }

    private void validateAggregationOf(CreateResourceRequest request) throws JsonProcessingException {
        if (request.getCredentialSubject().containsKey("gx:aggregationOf")) {
            JsonObject jsonObject = JsonParser.parseString(this.objectMapper.writeValueAsString(request)).getAsJsonObject();
            JsonArray aggregationArray = jsonObject
                    .getAsJsonObject("credentialSubject")
                    .getAsJsonArray("gx:aggregationOf");
            List<String> ids = new ArrayList<>();

            for (int i = 0; i < aggregationArray.size(); i++) {
                JsonObject aggregationObject = aggregationArray.get(i).getAsJsonObject();
                String idValue = aggregationObject.get("id").getAsString();
                ids.add(idValue);
            }
            this.signerService.validateRequestUrl(ids, "aggregation.of.not.found", Collections.singletonList("holderSignature"));
        }
    }

    public String resourceVc(CreateResourceRequest request, Participant participant, String name) throws JsonProcessingException {
        String id = this.wizardHost + participant.getId() + "/" + name + ".json";
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Map<String, Object> resourceRequest = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        resourceRequest.put("@context", this.contextConfig.resource());
        resourceRequest.put("type", Collections.singleton("VerifiableCredential"));
        resourceRequest.put("id", id);
        resourceRequest.put("issuer", participant.getDid());
        resourceRequest.put("issuanceDate", issuanceDate);
        Map<String, Object> credentialSub = request.getCredentialSubject();
        if (credentialSub != null) {
            credentialSub.put("@context", this.contextConfig.resource());
            credentialSub.put("id", id);
            if (request.getCredentialSubject().get("type").toString().contains("Physical")) {
                credentialSub.put("type", "gx:" + request.getCredentialSubject().get("type").toString());
            } else {
                if (!StringUtils.hasText(request.getCredentialSubject().get("subType").toString())) {
                    throw new BadDataException("sub.type.required");
                }
                credentialSub.put("type", "gx:" + request.getCredentialSubject().get("subType").toString());
                credentialSub.remove("subType");
                credentialSub.put("gx:policy", List.of(this.hostOdrlPolicy(participant)));
            }
        }
        resourceRequest.put("credentialSubject", credentialSub);
        map.put("resource", resourceRequest);
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("privateKey", HashingService.encodeToBase64(request.getPrivateKey()));
        resourceMap.put("issuer", participant.getDid());
        resourceMap.put("verificationMethod", request.getVerificationMethod());
        resourceMap.put("vcs", map);
        return this.signerService.signResource(resourceMap, participant.getId(), name);
    }

    public void hostResourceJson(String resourceJson, String hostedPath) {
        File file = new File("/tmp/" + hostedPath);
        try {
            FileUtils.writeStringToFile(file, resourceJson, Charset.defaultCharset());
            this.s3Utils.uploadFile(hostedPath, file);
        } catch (Exception e) {
            log.error("Error while hosting service offer json for participant:{},error:{}", hostedPath, e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }

    public PageResponse<ResourceFilterResponse> filterResource(FilterRequest filterRequest, String participantId) {

        if (StringUtils.hasText(participantId)) {
            FilterCriteria participantCriteria = new FilterCriteria(StringPool.PARTICIPANT_ID, Operator.CONTAIN, Collections.singletonList(participantId));
            List<FilterCriteria> filterCriteriaList = filterRequest.getCriteria() != null ? filterRequest.getCriteria() : new ArrayList<>();
            filterCriteriaList.add(participantCriteria);
            filterRequest.setCriteria(filterCriteriaList);
        }

        Page<Resource> resourcePage = this.filter(filterRequest);
        List<ResourceFilterResponse> resourceList = this.objectMapper.convertValue(resourcePage.getContent(), new TypeReference<>() {
        });

        return PageResponse.of(resourceList, resourcePage, filterRequest.getSort());
    }

    @Override
    protected BaseRepository<Resource, UUID> getRepository() {
        return this.repository;
    }

    @Override
    protected SpecificationUtil<Resource> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
