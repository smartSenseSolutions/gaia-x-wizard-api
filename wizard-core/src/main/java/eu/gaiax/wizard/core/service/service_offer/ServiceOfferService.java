package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.VerifiableCredential;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceOfferResponse;
import eu.gaiax.wizard.api.model.service_offer.SignerServiceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.CommonService;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.serviceoffer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    private final SignerClient signerClient;
    private final CredentialService credentialService;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ObjectMapper objectMapper;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;
    private final ContextConfig contextConfig;
    private final CommonService commonService;
    private final HashingService hashingService;
    private final S3Utils s3Utils;
    @Value("${wizard.domain}")
    private String domain;
    @Value("${wizard.host.wizard}")
    private String wizardHost;
    @Value("${wizard.gaiax.tnc}")
    private String tnc;

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
        Map<String, Object> values = new HashMap<>();
        values.put("values", rightOperand);
        constraintMap.put("rightOperand", values);
        constraint.add(constraintMap);
        perMap.put("constraint", constraint);
        permission.add(perMap);
        return permission;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public ServiceOfferResponse createServiceOffering(CreateServiceOfferingRequest request, String email) throws IOException {
        Map<String, Object> response = new HashMap<>();
        validateServiceOfferRequest(request);
        Participant participant;
        if (email != null) {
            participant = participantRepository.getByEmail(email);
            Credential participantCred = credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
            commonService.validateRequestUrl(participantCred.getVcUrl(), "participant.json.not.found");
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJsonUrl(), request.getVerificationMethod(), request.getPrivateKey(), request.getIssuer(), request.isStoreVault());
            participant = participantService.validateParticipant(participantValidatorRequest);
        }

        Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        String modifiedName = request.getName().replaceAll(" ", "_");
        String serviceName =  modifiedName + getRandomString();

        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (request.getCredentialSubject().containsKey("gx:policy")) {
            String policyId = participant.getId() + "/" + serviceName + "_policy";
            String policyUrl = this.wizardHost + policyId + ".json";
            Map<String, List<String>> policy = objectMapper.convertValue(request.getCredentialSubject().get("gx:policy"), Map.class);
            List<String> country = policy.get("gx:location");
            ODRLPolicyRequest odrlPolicyRequest = new ODRLPolicyRequest(country, "verifiableCredential.credentialSubject.legalAddress.country", participant.getDid(), participant.getDid(), wizardHost, serviceName);
            String hostPolicyJson = createODRLPolicy(odrlPolicyRequest, policyUrl);
            if (!org.apache.commons.lang3.StringUtils.isAllBlank(hostPolicyJson)) {
                hostODRLPolicy(hostPolicyJson, policyId);
                if (credentialSubject.containsKey("gx:policy")) {
                    credentialSubject.put("gx:policy", policyUrl);
                }
                credentialService.createCredential(hostPolicyJson, policyUrl, CredentialTypeEnum.ODRL_POLICY.getCredentialType(), "", participant);
            }
        }
        createTermsConditionHash(credentialSubject);
        request.setCredentialSubject(credentialSubject);
        String responseData = signService(participant, request, serviceName);
        String hostUrl = this.wizardHost + participant.getId() + "/" + serviceName + ".json";

        hostServiceOffer(responseData, participant.getId(), serviceName);

        Credential serviceOffVc = credentialService.createCredential(responseData, hostUrl, CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "", participant);
        ServiceOffer serviceOffer = ServiceOffer.builder()
                .name(request.getName())
                .participant(participant)
                .credential(serviceOffVc)
                .description(request.getDescription() == null ? "" : request.getDescription())
                .build();
        if (response.containsKey("veracityData")) {
            serviceOffer.setVeracityData(response.get("veracityData").toString());
        }
        serviceOffer = serviceOfferRepository.save(serviceOffer);
        TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
        };
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        List<Map<String, Object>> vc = objectMapper.readValue(serviceOffer.getCredential().getVcJson(), typeReference);
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
        String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
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
        String hostPolicyJson = objectMapper.writeValueAsString(ODRLPolicy);
        return hostPolicyJson;
    }


    private void createTermsConditionHash(Map<String, Object> credentialSubject) throws IOException {
        if (credentialSubject.containsKey("gx:termsAndConditions")) {
            Map<String, Object> termsAndConditions = objectMapper.convertValue(credentialSubject.get("gx:termsAndConditions"), Map.class);
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
            LOGGER.error("Error while hosting service offer json for participant:{}", hostedPath, e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }

    private String signService(Participant participant, CreateServiceOfferingRequest request, String serviceName) {
        Credential participantCred = credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        String id = this.wizardHost + participant.getId() + "/" + serviceName + ".json";
        Map<String, Object> providedBy = new HashMap<>();
        providedBy.put("id", request.getParticipantJsonUrl());
        request.getCredentialSubject().put("gx:providedBy", providedBy);
        request.getCredentialSubject().put("id", id);
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(contextConfig.serviceOffer())
                        .type(StringPool.VERIFIABLE_CREDENTIAL)
                        .id(participant.getDid())
                        .issuer(participant.getDid())
                        .issuanceDate(commonService.getCurrentFormattedDate())
                        .credentialSubject(request.getCredentialSubject())
                        .build()).build();
        List<VerifiableCredential> verifiableCredentialList = new ArrayList<>();
        verifiableCredentialList.add(verifiableCredential);
        SignerServiceRequest signerServiceRequest = SignerServiceRequest.builder()
                .privateKey(HashingService.encodeToBase64(request.getPrivateKey()))
                .issuer(participant.getDid())
                .legalParticipantURL(participantCred.getVcUrl())
                .verificationMethod(request.getVerificationMethod())
                .vcs(verifiableCredential)
                .build();
        try {
            ResponseEntity<Map<String, Object>> signerResponse = signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceOfferingString = objectMapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("completeSD"));
            LOGGER.debug("Send request to signer for service create vc");
            return serviceOfferingString;
        } catch (Exception e) {
            LOGGER.debug("Service vc not created", e.getMessage());
        }
        return null;
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
            Map<String,Object> termsCondition=objectMapper.convertValue(request.getCredentialSubject().get("gx:termsAndConditions"),Map.class);
            if(!termsCondition.containsKey("gx:URL")){
                throw new BadDataException("term.condition.not.found");
            }else{
                commonService.validateRequestUrl(termsCondition.get("gx:URL").toString(), "term.condition.not.found");
            }
        }
        if (!request.getCredentialSubject().containsKey("gx:aggregationOf") || StringUtils.hasText(request.getCredentialSubject().get("gx:aggregationOf").toString())) {
            throw new BadDataException("aggregation.of.not.found");
        } else {
            commonService.validateRequestUrl(request.getCredentialSubject().get("aggregation.of.not.found").toString(), "aggregation.of.not.found");
        }

        if (!request.getCredentialSubject().containsKey("gx:dataAccountExport")) {
            throw new BadDataException("data.account.export.not.found");
        } else {
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            Map<String, Object> export = objectMapper.readValue(request.getCredentialSubject().get("gx:dataAccountExport").toString(), typeReference);
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
            commonService.validateRequestUrl(request.getCredentialSubject().get("aggregation.of.not.found").toString(), "aggregation.of.not.found");
        }
    }
}
