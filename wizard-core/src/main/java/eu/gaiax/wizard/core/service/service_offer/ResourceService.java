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
import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
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

    private final ParticipantRepository participantRepository;

    private final ContextConfig contextConfig;

    private final ObjectMapper objectMapper;

    private final S3Utils s3Utils;

    private final CredentialService credentialService;

    private final SpecificationUtil<Resource> specificationUtil;

    private final SignerService signerService;

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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Resource createResource(CreateResourceRequest request, String id) throws JsonProcessingException {
        Participant participant;
        if (StringUtils.hasText(id)) {
            participant = this.participantRepository.findById(UUID.fromString(id)).orElse(null);
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.participantJson(), request.verificationMethod(), request.privateKey(), request.vault());
            participant = this.participantService.validateParticipant(participantValidatorRequest);
        }
        Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        this.validateResourceRequest(request);
        String hostUrl = participant.getId() + "/" + "resource_" + UUID.randomUUID() + ".json";
        String json = this.resourceVc(request, participant, this.wizardHost + hostUrl);
        if (StringUtils.hasText(json)) {
            this.hostResourceJson(json, hostUrl);
            Credential resourceVc = this.credentialService.createCredential(json, this.wizardHost + hostUrl, CredentialTypeEnum.RESOURCE.getCredentialType(), "", participant);
            Resource resource = Resource.builder().name(request.credentialSubject().get("gx:name").toString())
                    .credential(resourceVc)
                    .type(request.credentialSubject().get("type").toString())
                    .subType(request.credentialSubject().get("subType") == null ? null : request.credentialSubject().get("subType").toString())
                    .description(request.credentialSubject().get("gx:description") == null ? null : request.credentialSubject().get("gx:description").toString())
                    .participant(participant).build();
            return this.repository.save(resource);
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
        Validate.isFalse(StringUtils.hasText(request.credentialSubject().get("gx:name").toString())).launch("invalid.resource.name");
        this.validateAggregationOf(request);
    }

    private void validateAggregationOf(CreateResourceRequest request) throws JsonProcessingException {
        if (request.credentialSubject().containsKey("gx:aggregationOf")) {
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

    public String resourceVc(CreateResourceRequest request, Participant participant, String host) throws JsonProcessingException {
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Map<String, Object> resourceRequest = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        resourceRequest.put("@context", this.contextConfig.resource());
        resourceRequest.put("type", Collections.singleton("VerifiableCredential"));
        resourceRequest.put("id", host);
        resourceRequest.put("issuer", participant.getDid());
        resourceRequest.put("issuanceDate", issuanceDate);
        Map<String, Object> credentialSub = request.credentialSubject();
        if (credentialSub != null) {
            credentialSub.put("@context", this.contextConfig.resource());
            credentialSub.put("id", host);
            if (request.credentialSubject().get("type").toString().contains("Physical")) {
                credentialSub.put("type", "gx:" + request.credentialSubject().get("type").toString());
            } else {
                credentialSub.put("type", "gx:" + request.credentialSubject().get("subType").toString());
                credentialSub.remove("subType");
                credentialSub.put("gx:policy", List.of(this.hostOdrlPolicy(participant)));
            }
        }
        resourceRequest.put("credentialSubject", credentialSub);
        map.put("resource", resourceRequest);
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("privateKey", HashingService.encodeToBase64(request.privateKey()));
        resourceMap.put("issuer", participant.getDid());
        resourceMap.put("verificationMethod", request.verificationMethod());
        resourceMap.put("vcs", map);
        return this.signerService.signResource(resourceMap);
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

//        todo: resolve error (InvalidDataAccessApiUsageException: Can't compare test expression of type [BasicSqmPathSource(participantId : UUID)] with element of type [basicType@6(java.lang.String,12)])
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

    public PageResponse<ServiceAndResourceListDTO> getResourceList(FilterRequest filterRequest) {
        Page<Resource> resourcePage = this.filter(filterRequest);
        List<ServiceAndResourceListDTO> resourceList = this.objectMapper.convertValue(resourcePage.getContent(), new TypeReference<>() {
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
