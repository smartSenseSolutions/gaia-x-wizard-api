package eu.gaiax.wizard.core.service.ServiceOffer;

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
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.serviceoffer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.serviceoffer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceOfferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOfferService.class);

    private final SignerClient signerClient;
    private final CredentialService credentialService;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ObjectMapper objectMapper;
    private final ParticipantRepository participantRepository;
    private final ContextConfig contextConfig;
    private final CommonService commonService;
    private final HashingService hashingService;
    private final S3Utils s3Utils;

    @Transactional
    public ServiceOffer createServiceOffering(CreateServiceOfferingRequest request, String email) throws IOException {
        Map<String, Object> response = new HashMap<>();
        validateServiceOfferRequest(request);
        Map<String, Object> credentialSubject = request.getCredentialSubject();
        createTermsConditionHash(credentialSubject);
        Participant participant = participantRepository.getByEmail(email);
        Validate.isNull(participant).launch(new ParticipantNotFoundException("participant.not.found"));

        String responseData = singService(participant, request, response);

        try {
            String path = participant.getDomain() + "/" + participant.getDid();
            String hostUrl = hostServiceOffer(responseData, path, request.getName());
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
    private void createTermsConditionHash(Map<String, Object> credentialSubject) throws IOException {
        if (credentialSubject.containsKey("gx:termsAndConditions")) {
            Map<String, Object> termsAndConditions = objectMapper.convertValue(credentialSubject.get("gx:termsAndConditions"), Map.class);
            if (termsAndConditions.containsKey("gx:URL")) {
                String content=HashingService.fetchJsonContent(termsAndConditions.get("gx:URL").toString());
                termsAndConditions.put("gx:hash", hashingService.generateSha256Hash(content));
                credentialSubject.put("gx:termsAndConditions", termsAndConditions);
            }
        }
    }

    private String hostServiceOffer(String hostServiceOfferJson, String domain, String serviceName) throws IOException {
        File file = new File("/tmp/" + serviceName + ".json");
        FileUtils.writeStringToFile(file, hostServiceOfferJson, Charset.defaultCharset());
        String hostedPath = domain + "/" + serviceName + ".json";
        this.s3Utils.uploadFile(hostedPath, file);
        return this.s3Utils.getPreSignedUrl(hostedPath);
    }

    private String singService(Participant participant, CreateServiceOfferingRequest request, Map<String, Object> response) {
        Credential participantCred = credentialService.getByParticipantId(participant.getId());

        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(contextConfig.getServiceOffer())
                        .type(StringPool.VERIFIABLE_CREDENTIAL)
                        .id(participant.getDid())
                        .issuer(participant.getDid())
                        .issuanceDate(commonService.getCurrentFormattedDate())
                        .credentialSubject(request.getCredentialSubject())
                        .build()).build();
        List<VerifiableCredential> verifiableCredentialList = new ArrayList<>();
        verifiableCredentialList.add(verifiableCredential);


        SignerServiceRequest signerServiceRequest = SignerServiceRequest.builder()
                .privateKey(request.getPrivateKey())
                .issuer(participant.getDid())
                .legalParticipantURL(participantCred.getVcUrl())
                .verificationMethod(request.getVerificationMethod())
                .vcs(verifiableCredential)
                .build();
        try {
            ResponseEntity<Map<String, Object>> signerResponse = signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceOfferingString = objectMapper.writeValueAsString(((Map<String, Object>) signerResponse.getBody().get("data")).get("verifiablePresentation"));
            response = new JSONObject(serviceOfferingString).toMap();
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
