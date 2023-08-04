package eu.gaiax.wizard.core.service.ServiceOffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.VerifiableCredential;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
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
    private final String HOST_DOMAIN = "https://wizard-api.smart-x.smartsenselabs.com/.well-known";

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
        Map<String, Object> values = new HashMap<>();
        values.put("values", country);
        constraintMap.put("rightOperand", values);
        constraint.add(constraintMap);
        perMap.put("constraint", constraint);
        permission.add(perMap);
        return permission;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public ServiceOffer createServiceOffering(CreateServiceOfferingRequest request, String email) throws IOException {
        Map<String, Object> response = new HashMap<>();
        this.validateServiceOfferRequest(request);
        Participant participant;
        if (email != null) {
            participant = this.participantRepository.getByEmail(email);
        } else {
            //TODO needs to add DID
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJson(), request.getVerificationMethod(), request.getPrivateKey(), null, request.isStoreVault());
            participant = this.participantService.validateParticipant(participantValidatorRequest);
        }

        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));

        List<ServiceOffer> serviceOffers = this.serviceOfferRepository.findByName(request.getName());
        String serviceName = (serviceOffers.size() > 0 ? request.getName() + this.getRandomString() : request.getName());

        String policyId = participant.getId() + "/" + serviceName + "/ODRLPolicy";

        Map<String, Object> credentialSubject = request.getCredentialSubject();

        String hostPolicyJson = this.createOrdrlPolicy(request, participant.getDid(), participant.getDid(), policyId);
        if (!org.apache.commons.lang3.StringUtils.isAllBlank(hostPolicyJson)) {
            String policyUrl = this.hostODRLPolicy(hostPolicyJson, policyId, "ODRLPolicy#1");
            if (credentialSubject.containsKey("gx:policy")) {
                credentialSubject.put("gx:policy", this.HOST_DOMAIN + "/" + policyId + ".json");
            }
            this.credentialService.createCredential(hostPolicyJson, policyUrl, CredentialTypeEnum.ODRL_POLICY.getCredentialType(), "", participant);
        }
        this.createTermsConditionHash(credentialSubject);
        request.setCredentialSubject(credentialSubject);
        String responseData = this.singService(participant, request, serviceName);
        String hostUrl = this.hostServiceOffer(responseData, participant.getId(), serviceName);

/*
            String hostUrl="https://wizard-api.smart-x.smartsenselabs.com/d002ab48-64d6-41bc-83cd-87b43bebc6b6/Test/serv1";
*/
        Credential serviceOffVc = this.credentialService.createCredential(responseData, hostUrl, CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "", participant);
        ServiceOffer serviceOffer = ServiceOffer.builder()
                .name(request.getName())
                .participant(participant)
                .credential(serviceOffVc)
                .description(request.getDescription() == null ? "" : request.getDescription())
                .build();
        if (response.containsKey("veracityData")) {
            serviceOffer.setVeracityData(response.get("veracityData").toString());
        }
        serviceOffer = this.serviceOfferRepository.save(serviceOffer);
        return serviceOffer;

    }

    private String getRandomString() {
        final String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Create a Random object
        Random random = new Random();

        // Generate a random 4-character string
        StringBuilder randomString = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int randomIndex = random.nextInt(possibleCharacters.length());
            char randomChar = possibleCharacters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        System.out.println(randomString.toString());
        return randomString.toString();
    }


    private String createOrdrlPolicy(CreateServiceOfferingRequest request, String target, String assigner, String policyId) throws JsonProcessingException {
        if (request.getCredentialSubject().containsKey("gx:policy")) {
            Map<String, List<String>> policy = this.objectMapper.convertValue(request.getCredentialSubject().get("gx:policy"), Map.class);
            List<String> country = policy.get("gx:location");
            Map<String, Object> ODRLPolicy = new HashMap<>();
            //Add @context in the credential
            ODRLPolicy.put("@context", this.contextConfig.ODRLPolicy());
            ODRLPolicy.put("type", "policy");
            ODRLPolicy.put("id", this.HOST_DOMAIN + "/" + policyId);
            List<Map<String, Object>> permission = getMaps(country, target, assigner);
            ODRLPolicy.put("permission", permission);
            return this.objectMapper.writeValueAsString(ODRLPolicy);
        }
        return null;
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

    private String hostServiceOffer(String hostServiceOfferJson, UUID id, String serviceName) throws IOException {
        File file = new File("/tmp/" + serviceName + ".json");
        FileUtils.writeStringToFile(file, hostServiceOfferJson, Charset.defaultCharset());
        String hostedPath = id + "/" + serviceName + ".json";
        this.s3Utils.uploadFile(hostedPath, file);
        return this.s3Utils.getPreSignedUrl(serviceName + ".json");
    }

    private String hostODRLPolicy(String hostPolicyJson, String hostedPath, String policyName) throws IOException {
        File file = new File("/tmp/" + policyName + ".json");
        FileUtils.writeStringToFile(file, hostPolicyJson, Charset.defaultCharset());
        this.s3Utils.uploadFile(hostedPath + ".json", file);
        return this.s3Utils.getPreSignedUrl(policyName + ".json");
    }

    private String singService(Participant participant, CreateServiceOfferingRequest request, String serviceName) {
        Credential participantCred = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        String id = this.HOST_DOMAIN + "/" + participant.getId() + "/" + serviceName + ".json";
        Map<String, Object> providedBy = new HashMap<>();
        providedBy.put("id", "https://greenworld.proofsense.in/.well-known/participant.json");
        request.getCredentialSubject().put("gx:providedBy", providedBy);
        request.getCredentialSubject().put("id", id);
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(this.contextConfig.serviceOffer())
                        .type(StringPool.VERIFIABLE_CREDENTIAL)
                        .id(participant.getDid())
                        .issuer(participant.getDid())
                        .issuanceDate(this.commonService.getCurrentFormattedDate())
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
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceOfferingString = this.objectMapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("completeSD"));
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
