package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceIdRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceOfferResponse;
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
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ServiceOfferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOfferService.class);

    private final CredentialService credentialService;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ObjectMapper objectMapper;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;
    private final ContextConfig contextConfig;
    private final SignerService signerService;
    private final S3Utils s3Utils;
    private final PolicyService policyService;
    @Value("${wizard.host.wizard}")
    private String wizardHost;

    @NotNull
    private static List<Map<String, Object>> getMaps(List<String> rightOperand, String target, String assigner, String leftOperand) {
        List<Map<String, Object>> permission = new ArrayList<>();
        Map<String, Object> perMap = new HashMap<>();
        perMap.put("target", target);
        perMap.put("assigner", assigner);
        perMap.put("action", "view");
        List<Map<String, Object>> constraint = new ArrayList<>();
        Map<String, Object> constraintMap = new HashMap<>();
        constraintMap.put("leftOperand", leftOperand);
        constraintMap.put("operator", "isAnyOf");
        constraintMap.put("rightOperand", rightOperand);
        constraint.add(constraintMap);
        perMap.put("constraint", constraint);
        permission.add(perMap);
        return permission;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public ServiceOfferResponse createServiceOffering(CreateServiceOfferingRequest request, String email) throws IOException {
        Map<String, Object> response = new HashMap<>();
        this.validateServiceOfferRequest(request);
        Participant participant;
        if (email != null) {
            participant = this.participantRepository.getByEmail(email);
            Credential participantCred = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
            this.signerService.validateRequestUrl(Collections.singletonList(participantCred.getVcUrl()), "participant.json.not.found");
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJsonUrl(), request.getVerificationMethod(), request.getPrivateKey(), request.isStoreVault());
            participant = this.participantService.validateParticipant(participantValidatorRequest);
        }

        Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        String serviceName = "service_" + this.getRandomString();

        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (request.getCredentialSubject().containsKey("gx:policy")) {
            String policyId = participant.getId() + "/" + serviceName + "_policy";
            String policyUrl = this.wizardHost + policyId + ".json";
            Map<String, List<String>> policy = this.objectMapper.convertValue(request.getCredentialSubject().get("gx:policy"), Map.class);
            List<String> country = policy.get("gx:location");
            ODRLPolicyRequest odrlPolicyRequest = new ODRLPolicyRequest(country, "verifiableCredential.credentialSubject.legalAddress.country", participant.getDid(), participant.getDid(), this.wizardHost, serviceName);
            String hostPolicyJson = this.createODRLPolicy(odrlPolicyRequest, policyUrl);
            if (!org.apache.commons.lang3.StringUtils.isAllBlank(hostPolicyJson)) {
                this.hostODRLPolicy(hostPolicyJson, policyId);
                if (credentialSubject.containsKey("gx:policy")) {
                    credentialSubject.put("gx:policy", List.of(policyUrl));
                }
                this.credentialService.createCredential(hostPolicyJson, policyUrl, CredentialTypeEnum.ODRL_POLICY.getCredentialType(), "", participant);
            }
        }
        this.createTermsConditionHash(credentialSubject);
        request.setCredentialSubject(credentialSubject);

        String responseData = this.signerService.signService(participant, request, serviceName);
        String hostUrl = this.wizardHost + participant.getId() + "/" + serviceName + ".json";
        this.hostServiceOffer(responseData, participant.getId(), serviceName);

        Credential serviceOffVc = this.credentialService.createCredential(responseData, hostUrl, CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "", participant);
        ServiceOffer serviceOffer = ServiceOffer.builder()
                .name(request.getName())
                .participant(participant)
                .credential(serviceOffVc)
                .description(request.getDescription() == null ? "" : request.getDescription())
                .build();
        if (response.containsKey("trustIndex")) {
            serviceOffer.setVeracityData(response.get("trustIndex").toString());
        }
        serviceOffer = this.serviceOfferRepository.save(serviceOffer);
        TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
        };
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        List<Map<String, Object>> vc = this.objectMapper.readValue(serviceOffer.getCredential().getVcJson(), typeReference);
        ServiceOfferResponse serviceOfferResponse = ServiceOfferResponse.builder()
                .vcUrl(serviceOffer.getCredential().getVcUrl())
                .name(serviceOffer.getName())
                .veracityData(serviceOffer.getVeracityData())
                .vcJson(vc)
                .description(serviceOffer.getDescription())
                .build();
        return serviceOfferResponse;

    }

    private String getRandomString() {
        final String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            int randomIndex = random.nextInt(possibleCharacters.length());
            char randomChar = possibleCharacters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }


    public String createODRLPolicy(ODRLPolicyRequest odrlPolicyRequest, String hostUrl) throws IOException {
        Map<String, Object> ODRLPolicy = new HashMap<>();
        ODRLPolicy.put("@context", this.contextConfig.ODRLPolicy());
        ODRLPolicy.put("type", "policy");
        if (hostUrl == null) {
            hostUrl = odrlPolicyRequest.domain() + odrlPolicyRequest.target() + "/" + odrlPolicyRequest.serviceName() + "_policy.json";
        }
        ODRLPolicy.put("id", hostUrl);
        List<Map<String, Object>> permission = getMaps(odrlPolicyRequest.rightOperand(), odrlPolicyRequest.target(), odrlPolicyRequest.assigner(), odrlPolicyRequest.leftOperand());
        ODRLPolicy.put("permission", permission);
        String hostPolicyJson = this.objectMapper.writeValueAsString(ODRLPolicy);
        return hostPolicyJson;
    }


    private void createTermsConditionHash(Map<String, Object> credentialSubject) throws IOException {
        if (credentialSubject.containsKey("gx:termsAndConditions")) {
            Map<String, Object> termsAndConditions = this.objectMapper.convertValue(credentialSubject.get("gx:termsAndConditions"), Map.class);
            if (termsAndConditions.containsKey("gx:URL")) {
                String content = HashingService.fetchJsonContent(termsAndConditions.get("gx:URL").toString());
                termsAndConditions.put("gx:hash", HashingService.generateSha256Hash(content));
                credentialSubject.put("gx:termsAndConditions", termsAndConditions);
            }
        }
    }

    private void hostServiceOffer(String hostServiceOfferJson, UUID id, String serviceName) {
        File file = new File("/tmp/" + serviceName + ".json");
        try {
            FileUtils.writeStringToFile(file, hostServiceOfferJson, Charset.defaultCharset());
            String hostedPath = id + "/" + serviceName + ".json";
            this.s3Utils.uploadFile(hostedPath, file);
        } catch (Exception e) {
            LOGGER.error("Error while hosting service offer json for participant:{}", id, e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }

    private void hostODRLPolicy(String hostPolicyJson, String hostedPath) {
        File file = new File("/tmp/" + hostedPath + ".json");
        try {
            FileUtils.writeStringToFile(file, hostPolicyJson, Charset.defaultCharset());
            this.s3Utils.uploadFile(hostedPath + ".json", file);
        } catch (Exception e) {
            LOGGER.error("Error while hosting service offer json for participant:{},error:{}", hostedPath, e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }


    public void validateServiceOfferRequest(CreateServiceOfferingRequest request) {
        Validate.isFalse(StringUtils.hasText(request.getName())).launch("invalid.service.name");
        Validate.isTrue(CollectionUtils.isEmpty(request.getCredentialSubject())).launch("invalid.credential");
        Validate.isFalse(StringUtils.hasText(request.getPrivateKey())).launch("invalid.private.key");
    }

    public void validateServiceOfferMainRequest(CreateServiceOfferingRequest request) throws JsonProcessingException {
        Validate.isFalse(StringUtils.hasText(request.getName())).launch("invalid.service.name");
        Validate.isTrue(CollectionUtils.isEmpty(request.getCredentialSubject())).launch("invalid.credential");
        Validate.isFalse(StringUtils.hasText(request.getPrivateKey())).launch("invalid.private.key");
        if (!request.getCredentialSubject().containsKey("gx:termsAndConditions")) {
            throw new BadDataException("term.condition.not.found");
        } else {
            Map<String, Object> termsCondition = this.objectMapper.convertValue(request.getCredentialSubject().get("gx:termsAndConditions"), Map.class);
            if (!termsCondition.containsKey("gx:URL")) {
                throw new BadDataException("term.condition.not.found");
            } else {
                this.signerService.validateRequestUrl(Arrays.asList(termsCondition.get("gx:URL").toString()), "term.condition.not.found");
            }
        }
        if (!request.getCredentialSubject().containsKey("gx:aggregationOf") || StringUtils.hasText(request.getCredentialSubject().get("gx:aggregationOf").toString())) {
            throw new BadDataException("aggregation.of.not.found");
        } else {
            this.signerService.validateRequestUrl(Arrays.asList(request.getCredentialSubject().get("gx:aggregationOf").toString()), "aggregation.of.not.found");
        }

        if (!request.getCredentialSubject().containsKey("gx:dataAccountExport")) {
            throw new BadDataException("data.account.export.not.found");
        } else {
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            Map<String, Object> export = this.objectMapper.readValue(request.getCredentialSubject().get("gx:dataAccountExport").toString(), typeReference);
            if (!export.containsKey("gx:requestType") || StringUtils.hasText(export.get("gx:requestType").toString())) {
                throw new BadDataException("requestType.of.not.found");
            }
            if (!export.containsKey("gx:accessType") || StringUtils.hasText(export.get("gx:accessType").toString())) {
                throw new BadDataException("accessType.of.not.found");
            }
            if (!export.containsKey("gx:formatType") || StringUtils.hasText(export.get("gx:formatType").toString())) {
                throw new BadDataException("formatType.of.not.found");
            }
        }
        if (!request.getCredentialSubject().containsKey("gx:aggregationOf") || StringUtils.hasText(request.getCredentialSubject().get("gx:aggregationOf").toString())) {
            throw new BadDataException("aggregation.of.not.found");
        } else {
            List<Map<String, String>> agg = this.objectMapper.readValue(request.getCredentialSubject().get("aggregation.of.not.found").toString(), List.class);
            this.signerService.validateRequestUrl(Arrays.asList(request.getCredentialSubject().get("aggregationOf").toString()), "aggregation.of.not.found");
        }
    }

    public String[] getLocationFromService(ServiceIdRequest serviceIdRequest) {
        return this.policyService.getLocationByServiceOfferingId(serviceIdRequest.id());
    }
}
