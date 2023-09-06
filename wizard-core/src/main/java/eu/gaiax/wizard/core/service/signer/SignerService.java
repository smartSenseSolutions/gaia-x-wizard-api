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
import eu.gaiax.wizard.api.exception.SignerException;
import eu.gaiax.wizard.api.model.CreateVCRequest;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.ParticipantVerifyRequest;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.did.CreateDidRequest;
import eu.gaiax.wizard.api.model.did.ServiceEndpointConfig;
import eu.gaiax.wizard.api.model.did.ServiceEndpoints;
import eu.gaiax.wizard.api.model.did.ValidateDidRequest;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.service_offer.SignerServiceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.StringPool;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private final ContextConfig contextConfig;
    private final CredentialService credentialService;
    private final ParticipantRepository participantRepository;
    private final SignerClient signerClient;
    private final S3Utils s3Utils;
    private final ObjectMapper mapper;
    private final ScheduleService scheduleService;
    private final Vault vault;
    private final ServiceEndpointConfig serviceEndpointConfig;

    @Value("${wizard.signer-policies}")
    private List<String> policies;
    @Value("${wizard.host.wizard}")
    private String wizardHost;
    @Value("${wizard.gaiax.tnc}")
    private String tnc;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public void createParticipantJson(UUID participantId) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}", participantId);
        Participant participant = this.participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));

        if (this.credentialService.getLegalParticipantCredential(participant.getId()) != null) {
            log.info("Legal Participant exists for participantId {}. Exiting Legal Participant creation process", participantId);
            return;
        }

        this.createParticipantJson(participant, participant.getId().toString(), participant.isOwnDidSolution());
    }

    @SneakyThrows
    private Map<String, Object> prepareCredentialSubjectForLegalParticipant(Participant participant) {
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> Prepare credential subject for signer tool.");
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Map<String, Object> credential = this.mapper.readValue(participant.getCredentialRequest(), typeReference);
        Map<String, Object> legalParticipant = this.mapper.convertValue(credential.get("legalParticipant"), typeReference);
        Map<String, Object> legalRegistrationNumber = this.mapper.convertValue(credential.get("legalRegistrationNumber"), typeReference);
        //Add @context in the credential
        legalParticipant.put("@context", this.contextConfig.participant());
        legalParticipant.put("type", List.of("VerifiableCredential"));
        legalParticipant.put("id", participant.getDid());
        legalParticipant.put("issuer", participant.getDid());
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        legalParticipant.put("issuanceDate", issuanceDate);

        Map<String, Object> participantCredentialSubject = this.mapper.convertValue(legalParticipant.get("credentialSubject"), typeReference);
        String participantJsonUrl = this.formParticipantJsonUrl(participant.getDomain(), participant.getId());
        participantCredentialSubject.put("id", participantJsonUrl + "#0");
        participantCredentialSubject.put("type", "gx:LegalParticipant");
        String registrationId = participantJsonUrl + "#1";
        participantCredentialSubject.put("gx:legalRegistrationNumber", Map.of("id", registrationId));

        legalParticipant.put("credentialSubject", participantCredentialSubject);

        legalRegistrationNumber.put("@context", this.contextConfig.registrationNumber());
        legalRegistrationNumber.put("type", List.of("gx:legalRegistrationNumber"));
        legalRegistrationNumber.put("id", registrationId);

        Map<String, Object> tncVc = new TreeMap<>();
        tncVc.put("@context", this.contextConfig.tnc());
        tncVc.put("type", List.of("VerifiableCredential"));
        tncVc.put("id", participant.getDid());
        tncVc.put("issuer", participant.getDid());
        tncVc.put("issuanceDate", issuanceDate);

        Map<String, Object> tncCredentialSubject = new HashMap<>();
        tncCredentialSubject.put("type", "gx:GaiaXTermsAndConditions");
        tncCredentialSubject.put("@Context", this.contextConfig.tnc());
        tncCredentialSubject.put("id", participantJsonUrl + "#2");
        tncCredentialSubject.put("gx:termsAndConditions", this.tnc.replaceAll("\\\\n", "\n"));

        tncVc.put("credentialSubject", tncCredentialSubject);

        credential.put("legalParticipant", legalParticipant);
        credential.put("legalRegistrationNumber", legalRegistrationNumber);
        credential.put("gaiaXTermsAndConditions", tncVc);
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> CredentialSubject has been created successfully.");
        return credential;
    }

    private String formParticipantJsonUrl(String domain, UUID participantId) {
        if (StringUtils.hasText(domain)) {
            return "https://" + domain + "/" + participantId.toString() + "/participant.json";
        }
        return this.wizardHost + participantId.toString() + "/participant.json";
    }

    public void createParticipantJson(Participant participant, String key, boolean ownDid) {
        this.createParticipantJson(participant, participant.getDid(), participant.getDid(), key, ownDid);
    }

    public void createParticipantJson(Participant participant, String issuer, String verificationMethod, String key, boolean ownDid) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}, ownDid {}", participant.getId(), ownDid);
        File file = new File("/tmp/participant.json");
        try {
            String privateKey = key;
            if (!ownDid || participant.isKeyStored()) {
                privateKey = (String) this.vault.get(key).get("pkcs8.key");
                Validate.isFalse(StringUtils.hasText(privateKey)).launch(new EntityNotFoundException("keys.not.found"));
                log.info("SignerService(createParticipantJson) -> PrivateKey(pkcs8.key) resolve successfully from store with key {}", key);
            }
            Map<String, Object> credentials = this.prepareCredentialSubjectForLegalParticipant(participant);
            CreateVCRequest request = new CreateVCRequest(HashingService.encodeToBase64(privateKey), issuer, verificationMethod, credentials);
            log.info("SignerService(createParticipantJson) -> Initiate the signer client call to create legal participant json.");
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createVc(request);
            log.info("SignerService(createParticipantJson) -> Receive success response from signer tool.");
            String participantString = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(responseEntity.getBody()).get("data")).get("completeSD"));
            FileUtils.writeStringToFile(file, participantString, Charset.defaultCharset());
            String hostedPath = participant.getId() + "/participant.json";
            this.s3Utils.uploadFile(hostedPath, file);

            String participantJsonUrl = this.formParticipantJsonUrl(participant.getDomain(), participant.getId()) + "#0";
            this.credentialService.createCredential(participantString, participantJsonUrl, CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType(), null, participant);
            if (!ownDid) {
                this.addServiceEndpoint(participant.getId(), participantJsonUrl, this.serviceEndpointConfig.linkDomainType(), participantJsonUrl);
            }
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATED.getStatus());
            log.debug("SignerService(createParticipantJson) -> Participant json has been created for participant {} with selfDescription {}", participant.getId(), participantString);
        } catch (Exception e) {
            log.error("SignerService(createParticipantJson) -> Error while creating participant json for participant {}", participant.getId(), e);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
            CommonUtils.deleteFile(file);
            log.info("SignerService(createParticipantJson) -> Participant has been updated.");
        }
    }

    public void createDid(UUID participantId) {
        log.info("SignerService(createDid) -> Initiate process for creating did document for participant {}", participantId);

        Participant participant = this.participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        if (StringUtils.hasText(participant.getDid())) {
            log.info("DID exists for participantId {}. Exiting DID creation.", participantId);
            return;
        }

        File file = new File("/tmp/did.json");
        try {
            String domain = participant.getDomain();
            log.info("SignerService(createDid) ->  DID creation is initiated for domain {}", domain);
            CreateDidRequest createDidRequest = new CreateDidRequest(domain, List.of(new ServiceEndpoints(this.serviceEndpointConfig.pdpType(), this.serviceEndpointConfig.pdpUrl())));
            log.info("SignerService(createDid) -> Initiated signerClient call for create did for domain {}", domain);
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createDid(createDidRequest);
            log.info("SignerService(createDid): -> Response has been received from signerClient for domain {}", domain);
            String didString = this.mapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get("data")).get("did"));
            FileUtils.writeStringToFile(file, didString, Charset.defaultCharset());
            this.s3Utils.uploadFile(participant.getId() + "/did.json", file);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATED.getStatus());
            participant.setDid("did:web:" + domain);
            log.info("SignerService(createDid) -> DID Document has been created for participant {} with did {}", participant.getId(), participant.getDid());
            this.createParticipantCreationJob(participant);
        } catch (Exception e) {
            log.error("SignerService(createDid) -> Error while creating did json for enterprise -{}", participant.getDid(), e);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
            CommonUtils.deleteFile(file);
            log.info("SignerService(createDid) -> Participant details has been updated.");
        }
    }

    private void createParticipantCreationJob(Participant participant) {
        try {
            this.scheduleService.createJob(participant.getId().toString(), StringPool.JOB_TYPE_CREATE_PARTICIPANT, 0);
            log.info("SignerService(createParticipantCreationJob) -> Create legal participant corn has been scheduled.");
        } catch (Exception e) {
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
            log.error("SignerService(createParticipantCreationJob) -> Not able to create legal participant corn for participant {}", participant.getId(), e);
        }
    }

    public Map<String, String> signService(Participant participant, CreateServiceOfferingRequest request, String name) {
        Map<String, String> response = new HashMap<>();
        String id = this.wizardHost + participant.getId() + "/" + name + ".json";
        Map<String, Object> providedBy = new HashMap<>();
        providedBy.put("id", request.getParticipantJsonUrl());
        request.getCredentialSubject().put("gx:providedBy", providedBy);
        request.getCredentialSubject().put("id", id);
        request.getCredentialSubject().put("gx:name", request.getName());
        if (request.getDescription() != null) {
            request.getCredentialSubject().put("gx:description", request.getDescription());
        }
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(this.contextConfig.serviceOffer())
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
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceVc = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("completeSD"));
            String trustIndex = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("trustIndex"));
            response.put("serviceVc", serviceVc);
            if (trustIndex != null) {
                response.put("trustIndex", trustIndex);
            }
            if (serviceVc != null) {
                this.hostJsonFile(serviceVc, participant.getId(), name);
            }
            log.debug("Send request to signer for service create vc");
            return response;
        } catch (Exception e) {
            log.debug("Service vc not created", e.getMessage());
            throw new SignerException(e);
        }
    }

    public String signResource(Map<String, Object> resourceRequest, UUID id, String name) {
        try {
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.signResource(resourceRequest);
            String signResource = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("completeSD"));
            if (signResource != null) {
                this.hostJsonFile(signResource, id, name);
            }
            return signResource;
        } catch (Exception e) {
            log.debug("Service vc not created", e.getMessage());
            throw new SignerException(e.getMessage());
        }
    }

    private void hostJsonFile(String hostServiceOfferJson, UUID id, String name) {
        File file = new File("/tmp/" + name + ".json");
        try {
            FileUtils.writeStringToFile(file, hostServiceOfferJson, Charset.defaultCharset());
            String hostedPath = id + "/" + name + ".json";
            this.s3Utils.uploadFile(hostedPath, file);
        } catch (Exception e) {
            log.error("Error while hosting service offer json for participant:{}", id, e.getMessage());
            throw new BadDataException(e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }


    public String signLabelLevel(Map<String, Object> labelLevelRequest, UUID id, String name) {
        try {
            log.info(this.mapper.writeValueAsString(labelLevelRequest) + ">>>>>>>>>>>>.Label level");
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.signLabelLevel(labelLevelRequest);
            String signResource = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get("data")).get("selfDescriptionCredential"));
            if (signResource != null) {
                this.hostJsonFile(signResource, id, name);
            }
            return signResource;
        } catch (Exception e) {
            log.debug("Service vc not created", e.getMessage());
            throw new SignerException(e.getMessage());
        }
    }

    public void validateRequestUrl(List<String> urls, String message, List<String> policy) {
        AtomicReference<ParticipantVerifyRequest> participantValidatorRequest = new AtomicReference<>();
        if (policy == null) {
            policy = this.policies;
        }

        List<String> finalPolicy = policy;
        urls.parallelStream().forEach(url -> {
            try {
                participantValidatorRequest.set(new ParticipantVerifyRequest(url, finalPolicy));
                ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.verify(participantValidatorRequest.get());
                log.debug("signer validation response: {}", Objects.requireNonNull(signerResponse.getBody()).get("message").toString());
            } catch (Exception e) {
                log.error("An error occurred for URL: " + url, e);
                throw new BadDataException(message + ",url=" + url);
            }
        });
    }

    public void addServiceEndpoint(UUID participantId, String id, String type, String url) {
        Map<String, String> map = Map.of("id", id, "type", type, "serviceEndpoints", url);
        String didPath = "/tmp/" + UUID.randomUUID() + ".json";
        File file = null;
        File updatedFile = new File("/tmp/" + UUID.randomUUID() + ".json");
        try {
            file = this.s3Utils.getObject(participantId + "/did.json", didPath);
            String didString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(didString);
            JSONArray services = jsonObject.optJSONArray("service");
            if (Objects.isNull(services)) {
                jsonObject.put("service", new ArrayList<>());
                services = jsonObject.getJSONArray("service");
            }
            List<String> serviceIds = new ArrayList<>();
            for (Object service : services) {
                JSONObject s = (JSONObject) service;
                serviceIds.add(s.getString("id"));
            }
            if (!serviceIds.contains(id)) {
                services.put(map);
            }
            FileUtils.writeStringToFile(updatedFile, jsonObject.toString(), Charset.defaultCharset());
            this.s3Utils.uploadFile(participantId + "/did.json", updatedFile);
        } catch (Exception ex) {
            log.error("Issue occurred while add service endpoint into the DID document for participant {}", participantId);
            throw new BadDataException("not.able.to.add.service.endpoint");
        } finally {
            CommonUtils.deleteFile(file, updatedFile);
        }
    }

    public boolean validateDid(String issuerDid, String verificationMethod, String privateKey) {
        try {
            ValidateDidRequest request = new ValidateDidRequest(issuerDid, verificationMethod, HashingService.encodeToBase64(privateKey));
            ResponseEntity<Map<String, Object>> response = this.signerClient.validateDid(request);
            boolean valid = (boolean) ((Map<String, Object>) Objects.requireNonNull(response.getBody()).get("data")).get("isValid");
            return valid;
        } catch (Exception ex) {
            log.error("Issue occurred while validating did {} with verification method {}", issuerDid, verificationMethod);
            return false;
        }
    }

    @Deprecated
    public void createDid(String domain) {
        log.info("SignerService(createDid) -> Initiate process for creating did document for domain {}", domain);
        File file = new File("/tmp/did.json");
        try {
            log.info("SignerService(createDid) ->  DID creation is initiated for domain {}", domain);
            CreateDidRequest createDidRequest = new CreateDidRequest(domain, List.of(new ServiceEndpoints(this.serviceEndpointConfig.pdpType(), this.serviceEndpointConfig.pdpUrl())));
            log.info("SignerService(createDid) -> Initiated signerClient call for create did for domain {}", domain);
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createDid(createDidRequest);
            log.info("SignerService(createDid): -> Response has been received from signerClient for domain {}", domain);
            String didString = this.mapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get("data")).get("did"));
            FileUtils.writeStringToFile(file, didString, Charset.defaultCharset());
            this.s3Utils.uploadFile(domain + "/did.json", file);
            log.info("SignerService(createDid) -> DID Document has been created for domain  {}", domain);
        } catch (Exception e) {
            log.error("SignerService(createDid) -> Error while creating did json for domain -{}", domain, e);
            throw new BadDataException("did.creation.failed");
        } finally {
            CommonUtils.deleteFile(file);
            log.info("SignerService(createDid) -> Participant details has been updated.");
        }
    }

    public boolean validateRegistrationNumber(Map<String, Object> credential) {
        try {
            Map<String, Object> request = new HashMap<>();
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            Map<String, Object> legalRegistrationNumber = this.mapper.convertValue(credential.get("legalRegistrationNumber"), typeReference);
            legalRegistrationNumber.put("@context", this.contextConfig.registrationNumber());
            legalRegistrationNumber.put("type", "gx:legalRegistrationNumber");
            legalRegistrationNumber.put("id", "did:web:gaia-x.eu:legalRegistrationNumber.json");
            request.put("legalRegistrationNumber", legalRegistrationNumber);
            ResponseEntity<Map<String, Object>> response = this.signerClient.validateRegistrationNumber(request);
            boolean valid = (boolean) ((Map<String, Object>) Objects.requireNonNull(response.getBody()).get("data")).get("isValid");
            return valid;
        } catch (Exception ex) {
            log.error("Issue occurred while validating the registration number.", ex);
            return false;
        }
    }
}
