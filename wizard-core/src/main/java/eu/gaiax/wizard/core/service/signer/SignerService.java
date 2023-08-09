/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.signer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.VerifiableCredential;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.service_offer.SignerServiceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Signer service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignerService {

    private static final List<String> policies = Arrays.asList(
            "integrityCheck",
            "holderSignature",
            "complianceSignature",
            "complianceCheck"
    );
    private final ContextConfig contextConfig;
    private final CredentialService credentialService;
    private final ParticipantRepository participantRepository;
    private final SignerClient signerClient;
    private final S3Utils s3Utils;
    private final ObjectMapper mapper;
    private final ScheduleService scheduleService;
    private final Vault vault;
    @Value("${wizard.host.wizard}")
    private String wizardHost;
    @Value("${wizard.gaiax.tnc}")
    private String tnc;

    public void createParticipantJson(UUID participantId) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}", participantId);
        Participant participant = participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        prepareCredentialSubjectForLegalParticipant(participant);
        createParticipantJson(participant, participant.getId().toString(), participant.isOwnDidSolution());
    }

    @SneakyThrows
    private Map<String, Object> prepareCredentialSubjectForLegalParticipant(Participant participant) {
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> Prepare credential subject for signer tool.");
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Map<String, Object> credential = mapper.readValue(participant.getCredential(), typeReference);
        Map<String, Object> legalParticipant = mapper.convertValue(credential.get("legalParticipant"), typeReference);
        Map<String, Object> legalRegistrationNumber = mapper.convertValue(credential.get("legalRegistrationNumber"), typeReference);
        //Add @context in the credential
        legalParticipant.put("@context", contextConfig.participant());
        legalParticipant.put("type", List.of("VerifiableCredential"));
        legalParticipant.put("id", participant.getDid());
        legalParticipant.put("issuer", participant.getDid());
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        legalParticipant.put("issuanceDate", issuanceDate);

        Map<String, Object> participantCredentialSubject = mapper.convertValue(legalParticipant.get("credentialSubject"), typeReference);
        participantCredentialSubject.put("id", formParticipantJsonUrl(participant.getId()) + "#0");
        participantCredentialSubject.put("type", "gx:LegalParticipant");
        String registrationId = formParticipantJsonUrl(participant.getId()) + "#1";
        participantCredentialSubject.put("gx:legalRegistrationNumber", Map.of("id", registrationId));

        legalParticipant.put("credentialSubject", participantCredentialSubject);

        legalRegistrationNumber.put("@context", contextConfig.registrationNumber());
        legalRegistrationNumber.put("type", List.of("gx:legalRegistrationNumber"));
        legalRegistrationNumber.put("id", registrationId);

        Map<String, Object> tncVc = new TreeMap<>();
        tncVc.put("@context", contextConfig.tnc());
        tncVc.put("type", List.of("VerifiableCredential"));
        tncVc.put("id", participant.getDid());
        tncVc.put("issuer", participant.getDid());
        tncVc.put("issuanceDate", issuanceDate);

        Map<String, Object> tncCredentialSubject = new HashMap<>();
        tncCredentialSubject.put("type", "gx:GaiaXTermsAndConditions");
        tncCredentialSubject.put("@Context", contextConfig.tnc());
        tncCredentialSubject.put("id", formParticipantJsonUrl(participant.getId()) + "#2");
        tncCredentialSubject.put("gx:termsAndConditions", tnc.replaceAll("\\\\n", "\n"));

        tncVc.put("credentialSubject", tncCredentialSubject);

        credential.put("legalParticipant", legalParticipant);
        credential.put("legalRegistrationNumber", legalRegistrationNumber);
        credential.put("gaiaXTermsAndConditions", tncVc);
        participant.setCredential(mapper.writeValueAsString(credential));
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> CredentialSubject has been created successfully.");
        return credential;
    }

    private String formParticipantJsonUrl(UUID participantId) {
        return wizardHost + participantId.toString() + "/participant.json";
    }

    public void createParticipantJson(Participant participant, String key, boolean ownDid) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}, ownDid {}", participant.getId(), ownDid);
        File file = new File("/tmp/participant.json");
        try {
            String privateKey = key;
            if (!ownDid) {
                privateKey = (String) vault.get(key).get("pkcs8.key");
                Validate.isFalse(StringUtils.hasText(privateKey)).launch(new EntityNotFoundException("keys.not.found"));
                log.info("SignerService(createParticipantJson) -> PrivateKey(pkcs8.key) resolve successfully from vault with key {}", key);
            }
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            Map<String, Object> credentials = mapper.readValue(participant.getCredential(), typeReference);
            CreateVCRequest request = new CreateVCRequest(HashingService.encodeToBase64(privateKey), participant.getDid(), participant.getDid(), credentials);
            log.info("SignerService(createParticipantJson) -> Initiate the signer client call to create legal participant json.");
            ResponseEntity<Map<String, Object>> responseEntity = signerClient.createVc(request);
            log.info("SignerService(createParticipantJson) -> Receive success response from signer tool.");
            String participantString = mapper.writeValueAsString(responseEntity.getBody().get("data"));
            FileUtils.writeStringToFile(file, participantString, Charset.defaultCharset());
            String hostedPath = participant.getId() + "/participant.json";
            s3Utils.uploadFile(hostedPath, file);
            credentialService.createCredential(participantString, formParticipantJsonUrl(participant.getId()), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType(), null, participant);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATED.getStatus());
            log.debug("SignerService(createParticipantJson) -> Participant json has been created for participant {} with selfDescription {}", participant.getId(), participantString);
        } catch (Exception e) {
            log.error("SignerService(createParticipantJson) -> Error while creating participant json for participant {}", participant.getId(), e);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
        } finally {
            participantRepository.save(participant);
            CommonUtils.deleteFile(file);
            log.info("SignerService(createParticipantJson) -> Participant has been updated.");
        }
    }

    public void createDid(UUID participantId) {
        log.info("SignerService(createDid) -> Initiate process for creating did document for participant {}", participantId);
        File file = new File("/tmp/did.json");
        Participant participant = participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        try {
            String domain = participant.getDomain();
            log.info("SignerService(createDid) ->  DID creation is initiated for domain {}", domain);
            CreateDidRequest createDidRequest = new CreateDidRequest(domain);
            log.info("SignerService(createDid) -> Initiated signerClient call for create did for domain {}", domain);
            ResponseEntity<Map<String, Object>> responseEntity = signerClient.createDid(createDidRequest);
            log.info("SignerService(createDid): -> Response has been received from signerClient for domain {}", domain);
            String didString = mapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get("data")).get("did"));
            FileUtils.writeStringToFile(file, didString, Charset.defaultCharset());
            s3Utils.uploadFile(participant.getId() + "/did.json", file);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATED.getStatus());
            participant.setDid("did:web:" + domain);
            log.info("SignerService(createDid) -> DID Document has been created for participant {} with did {}", participant.getId(), participant.getDid());
            createParticipantCreationJob(participant);
        } catch (Exception e) {
            log.error("SignerService(createDid) -> Error while creating did json for enterprise -{}", participant.getDid(), e);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATION_FAILED.getStatus());
        } finally {
            participantRepository.save(participant);
            CommonUtils.deleteFile(file);
            log.info("SignerService(createDid) -> Participant details has been updated.");
        }
    }

    private void createParticipantCreationJob(Participant participant) {
        try {
            scheduleService.createJob(participant.getId().toString(), StringPool.JOB_TYPE_CREATE_PARTICIPANT, 0);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
            log.info("SignerService(createParticipantCreationJob) -> Create legal participant corn has been scheduled.");
        } catch (Exception e) {
            log.error("SignerService(createParticipantCreationJob) -> Not able to create legal participant corn for participant {}", participant.getId(), e);
        }
    }

    public String signService(Participant participant, CreateServiceOfferingRequest request, String serviceName) {
        String id = wizardHost + participant.getId() + "/" + serviceName + ".json";
        Map<String, Object> providedBy = new HashMap<>();
        providedBy.put("id", request.getParticipantJsonUrl() + "#0");
        request.getCredentialSubject().put("gx:providedBy", providedBy);
        request.getCredentialSubject().put("id", id);
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(contextConfig.serviceOffer())
                        .type(StringPool.VERIFIABLE_CREDENTIAL)
                        .id(participant.getDid())
                        .issuer(participant.getDid())
                        .issuanceDate(issuanceDate)
                        .credentialSubject(request.getCredentialSubject())
                        .build()).build();
        List<VerifiableCredential> verifiableCredentialList = new ArrayList<>();
        verifiableCredentialList.add(verifiableCredential);
        SignerServiceRequest signerServiceRequest = SignerServiceRequest.builder()
                .privateKey(HashingService.encodeToBase64(request.getPrivateKey()))
                .issuer(participant.getDid())
                .verificationMethod(request.getVerificationMethod())
                .vcs(verifiableCredential)
                .build();
        try {
            ResponseEntity<Map<String, Object>> signerResponse = signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceOfferingString = mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("completeSD"));
            log.debug("Send request to signer for service create vc");
            return serviceOfferingString;
        } catch (Exception e) {
            log.debug("Service vc not created", e.getMessage());
        }
        return null;
    }

    public void validateRequestUrl(List<String> urls, String message) {
        try {
            AtomicReference<ParticipantVerifyRequest> participantValidatorRequest = null;
            urls.parallelStream().forEach(url -> {
                participantValidatorRequest.set(new ParticipantVerifyRequest(url, policies));
                ResponseEntity<Map<String, Object>> signerResponse = signerClient.verify(participantValidatorRequest.get());
                log.debug("signer validation response:{}", signerResponse.getBody().get("message").toString());
            });
        } catch (Exception e) {
            throw new BadDataException(message);
        }
    }
}
