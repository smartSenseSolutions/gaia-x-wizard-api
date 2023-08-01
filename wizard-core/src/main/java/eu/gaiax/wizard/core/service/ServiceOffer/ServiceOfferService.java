package eu.gaiax.wizard.core.service.ServiceOffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.VerifiableCredential;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.ParticipantNotFoundException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.ServiceOffer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.ServiceOffer.SignerServiceRequest;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.CommonService;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.serviceoffer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.serviceoffer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
    private final String HOST_DOMAIN="https://wizard-api.smart-x.smartsenselabs.com";

    @NotNull
    private static List<Map<String, Object>> getMaps(List<String> country, String target, String assigner) {
        List<Map<String, Object>> permission = new ArrayList<>();
        Map<String, Object> perMap = new HashMap<>();
        perMap.put("target", target);
        perMap.put("assigner", assigner);
        perMap.put("action", "view");
        List<Map<String, Object>> constraint = new ArrayList<>();
        Map<String, Object> constraintMap = new HashMap<>();
        constraintMap.put("leftOperand", "verifiableCredential.credentialSubject.legalAddress.country");
        constraintMap.put("operator", "isAnyOf");
        constraintMap.put("rightOperand", "{values:" + country + "}");
        constraint.add(constraintMap);
        perMap.put("constraint", constraint);
        permission.add(perMap);
        return permission;
    }

    @Transactional
    public ServiceOffer createServiceOffering(CreateServiceOfferingRequest request, String email) throws IOException {
        Map<String, Object> response = new HashMap<>();
        validateServiceOfferRequest(request);
        Participant participant;
        if (request.getParticipantJson() == null) {
            participant = participantRepository.getByEmail(email);
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJson(), request.getVerificationMethod(), request.getPrivateKey(), request.isStoreVault());
            participant=participantService.validateParticipant(participantValidatorRequest);
        }

        Validate.isNull(participant).launch(new ParticipantNotFoundException("participant.not.found"));

        List<ServiceOffer> serviceOffers = serviceOfferRepository.findByName(request.getName());
        String serviceName = (!CollectionUtils.isEmpty(serviceOffers) ? request.getName() : request.getName() + UUID.randomUUID());

        String domain = HOST_DOMAIN+"/" + participant.getId();
        String policyId = domain + "/" + serviceName + "/ODRLPolicy#1";

        Map<String, Object> credentialSubject = request.getCredentialSubject();

        String hostPolicyJson = createOrdrlPolicy(request, participant.getDid(), participant.getDid(), policyId);
        if (org.apache.commons.lang3.StringUtils.isAllBlank(hostPolicyJson)) {
            String policyUrl = hostODRLPolicy(hostPolicyJson, domain, policyId, "ODRLPolicy#1", credentialSubject);
            if (credentialSubject.containsKey("gx:policy")) {
                credentialSubject.put("gx:policy", policyUrl);
            }
        }

        createTermsConditionHash(credentialSubject);

        try {
            String responseData = singService(participant, request, response, domain, serviceName);
            String hostUrl = hostServiceOffer(responseData, domain, serviceName);
            Credential serviceOffVc = credentialService.createCredential(responseData, hostUrl, CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "", participant);
            ServiceOffer serviceOffer = ServiceOffer.builder()
                    .name(request.getName())
                    .credentialId(serviceOffVc.getId())
                    .description(request.getDescription())
                    .veracityData(response.get("veracityData").toString())
                    .build();
            serviceOfferRepository.save(serviceOffer);
            return serviceOffer;
        } catch (Exception e) {
            LOGGER.debug("Service vc not created", e.getMessage());
        }
        return null;
    }

    private String createOrdrlPolicy(CreateServiceOfferingRequest request, String target, String assigner, String policyId) throws JsonProcessingException {
        if (request.getCredentialSubject().containsKey("gx:policy")) {
            Map<String, List<String>> policy = objectMapper.convertValue(request.getCredentialSubject().get("gx:policy"), Map.class);
            List<String> country = policy.get("gx:location");
            Map<String, Object> ODRLPolicy = new HashMap<>();
            //Add @context in the credential
            ODRLPolicy.put("@context", this.contextConfig.ODRLPolicy());
            ODRLPolicy.put("type", "policy");
            ODRLPolicy.put("id", policyId);
            List<Map<String, Object>> permission = getMaps(country, target, assigner);
            ODRLPolicy.put("permission", permission);
            return objectMapper.writeValueAsString(ODRLPolicy);
        }
        return null;
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

    private String hostServiceOffer(String hostServiceOfferJson, String domain, String serviceName) throws IOException {
        File file = new File("/tmp/" + serviceName + ".json");
        FileUtils.writeStringToFile(file, hostServiceOfferJson, Charset.defaultCharset());
        String hostedPath = domain + "/" + serviceName + ".json";
        this.s3Utils.uploadFile(domain, file);
        return this.s3Utils.getUploadUrl(hostedPath);
    }

    private String hostODRLPolicy(String hostPolicyJson, String domain, String hostedPath, String policyName, Map<String, Object> credentialSubject) throws IOException {
        File file = new File("/tmp/" + policyName + ".json");
        FileUtils.writeStringToFile(file, hostPolicyJson, Charset.defaultCharset());
        this.s3Utils.uploadFile(domain, file);
        return this.s3Utils.getUploadUrl(hostedPath);
    }

    private String singService(Participant participant, CreateServiceOfferingRequest request, Map<String, Object> response, String domain, String serviceName) {
        Credential participantCred = credentialService.getByParticipantId(participant.getId());
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(contextConfig.serviceOffer())
                        .type(StringPool.VERIFIABLE_CREDENTIAL)
                        .id(domain + "/" + serviceName)
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
            String serviceOfferingString = objectMapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("verifiablePresentation"));
            LOGGER.debug("Send request to signer for service create vc");
            return serviceOfferingString;
        } catch (Exception e) {
            LOGGER.debug("Service vc not created", e.getMessage());
        }
        return null;
    }

    private void validateServiceOfferRequest(CreateServiceOfferingRequest request) {
        Validate.isFalse(StringUtils.hasText(request.getName())).launch("invalid.service.name");
        Validate.isTrue(CollectionUtils.isEmpty(request.getCredentialSubject())).launch("invalid.credential");
        Validate.isFalse(StringUtils.hasText(request.getPrivateKey())).launch("invalid.private.key");
    }
}
