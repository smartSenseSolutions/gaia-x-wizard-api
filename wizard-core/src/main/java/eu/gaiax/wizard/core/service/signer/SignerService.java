/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.signer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.VerifiableCredential;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.ConflictException;
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
import eu.gaiax.wizard.core.service.participant.InvokeService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
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

import static eu.gaiax.wizard.api.utils.StringPool.*;

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
    private final ServiceEndpointConfig serviceEndpointConfig;
    private final MessageSource messageSource;

    @Value("${wizard.signer-policies}")
    private List<String> policies;
    @Value("${wizard.host.wizard}")
    private String wizardHost;
    @Value("${wizard.gaiax.tnc}")
    private String tnc;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public void createParticipantJson(UUID participantId) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}", participantId);
        Participant participant = this.participantRepository.findById(participantId).orElseThrow(() -> new EntityNotFoundException("participant.not.found"));

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
        Map<String, Object> legalParticipant = this.mapper.convertValue(credential.get(LEGAL_PARTICIPANT), typeReference);
        Map<String, Object> legalRegistrationNumber = this.mapper.convertValue(credential.get(LEGAL_REGISTRATION_NUMBER), typeReference);
        //Add @context in the credential
        legalParticipant.put(CONTEXT, this.contextConfig.participant());
        String participantJsonUrl = this.formParticipantJsonUrl(participant.getDomain(), participant.getId());

        legalParticipant.put(TYPE, List.of(VERIFIABLE_CREDENTIAL));
        legalParticipant.put(ID, participantJsonUrl + "#0");
        legalParticipant.put(ISSUER, participant.getDid());
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        legalParticipant.put(ISSUANCE_DATE, issuanceDate);

        Map<String, Object> participantCredentialSubject = this.mapper.convertValue(legalParticipant.get(CREDENTIAL_SUBJECT), typeReference);
        participantCredentialSubject.put(ID, participantJsonUrl + "#0");
        participantCredentialSubject.put(TYPE, GX_LEGAL_PARTICIPANT);
        String registrationId = participantJsonUrl + "#1";
        participantCredentialSubject.put(GX_LEGAL_REGISTRATION_NUMBER, Map.of(ID, registrationId));

        legalParticipant.put(CREDENTIAL_SUBJECT, participantCredentialSubject);

        legalRegistrationNumber.put(CONTEXT, this.contextConfig.registrationNumber());
        legalRegistrationNumber.put(TYPE, List.of(GX_LEGAL_REGISTRATION_NUMBER));
        legalRegistrationNumber.put(ID, registrationId);

        Map<String, Object> tncVc = new TreeMap<>();
        tncVc.put(CONTEXT, this.contextConfig.tnc());
        tncVc.put(TYPE, List.of(VERIFIABLE_CREDENTIAL));
        tncVc.put(ID, participantJsonUrl + "#2");
        tncVc.put(ISSUER, participant.getDid());
        tncVc.put(ISSUANCE_DATE, issuanceDate);

        Map<String, Object> tncCredentialSubject = new HashMap<>();
        tncCredentialSubject.put(TYPE, "gx:GaiaXTermsAndConditions");
        tncCredentialSubject.put(CONTEXT, this.contextConfig.tnc());
        tncCredentialSubject.put(ID, participantJsonUrl + "#2");
        tncCredentialSubject.put(GX_TERMS_AND_CONDITIONS, this.tnc.replace("\\\\n", "\n"));

        tncVc.put(CREDENTIAL_SUBJECT, tncCredentialSubject);

        credential.put(LEGAL_PARTICIPANT, legalParticipant);
        credential.put(LEGAL_REGISTRATION_NUMBER, legalRegistrationNumber);
        credential.put("gaiaXTermsAndConditions", tncVc);
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> CredentialSubject has been created successfully.");
        return credential;
    }

    private String formParticipantJsonUrl(String domain, UUID participantId) {
        if (StringUtils.hasText(domain)) {
            return "https://" + domain + "/" + participantId.toString() + "/" + PARTICIPANT_JSON;
        }
        return this.wizardHost + participantId.toString() + "/" + PARTICIPANT_JSON;
    }

    public void createParticipantJson(Participant participant, String key, boolean ownDid) {
        this.createParticipantJson(participant, participant.getDid(), participant.getDid(), key, ownDid);
    }

    public void createParticipantJson(Participant participant, String issuer, String verificationMethod, String key, boolean ownDid) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}, ownDid {}", participant.getId(), ownDid);
        File file = new File(TEMP_FOLDER + "participant.json");
        try {
            boolean isVault = false;
            String privateKey = key;
            if (!ownDid || participant.isKeyStored()) {
                isVault = true;
                log.info("SignerService(createParticipantJson) -> PrivateKey(pkcs8.key) resolve successfully from store with key {}", key);
            } else {
                privateKey = HashingService.encodeToBase64(privateKey);
            }
            Map<String, Object> credentials = this.prepareCredentialSubjectForLegalParticipant(participant);
            CreateVCRequest request = new CreateVCRequest(privateKey, issuer, verificationMethod, credentials, isVault);
            log.info("SignerService(createParticipantJson) -> Initiate the signer client call to create legal participant json.");
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createVc(request);
            log.info("SignerService(createParticipantJson) -> Receive success response from signer tool.");
            String participantString = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(responseEntity.getBody()).get(DATA)).get(COMPLETE_SD));
            FileUtils.writeStringToFile(file, participantString, Charset.defaultCharset());
            String hostedPath = participant.getId() + "/" + PARTICIPANT_JSON;
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

        Participant participant = this.participantRepository.findById(participantId).orElseThrow(() -> new EntityNotFoundException("participant.not.found"));
        if (StringUtils.hasText(participant.getDid())) {
            log.info("DID exists for participantId {}. Exiting DID creation.", participantId);
            return;
        }

        if (!this.fetchX509Certificate(participant.getDomain())) {
            this.createDidCreationJob(participant);
            return;
        }

        File file = new File(TEMP_FOLDER + DID_JSON);
        try {
            String domain = participant.getDomain();
            log.info("SignerService(createDid) ->  DID creation is initiated for domain {}", domain);
            CreateDidRequest createDidRequest = new CreateDidRequest(domain, List.of(new ServiceEndpoints(this.serviceEndpointConfig.pdpType(), this.serviceEndpointConfig.pdpUrl())));
            log.info("SignerService(createDid) -> Initiated signerClient call for create did for domain {}", domain);
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createDid(createDidRequest);
            log.info("SignerService(createDid): -> Response has been received from signerClient for domain {}", domain);
            String didString = this.mapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get(DATA)).get("did"));
            FileUtils.writeStringToFile(file, didString, Charset.defaultCharset());
            this.s3Utils.uploadFile(participant.getId() + "/" + DID_JSON, file);
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

    private boolean fetchX509Certificate(String domain) {
        try {
            String x509Certificate = InvokeService.executeRequest("https://" + domain + "/.well-known/x509CertificateChain.pem", HttpMethod.GET);
            Validate.isFalse(StringUtils.hasText(x509Certificate)).launch("x509certificate.not.resolved");
            return true;
        } catch (Exception ex) {
            log.error("Not able to fetch x509 certificate for domain {}", domain, ex);
            return false;
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
        String id = this.wizardHost + participant.getId() + "/" + name + JSON_EXTENSION;
        Map<String, Object> providedBy = new HashMap<>();
        providedBy.put(ID, request.getParticipantJsonUrl());
        request.getCredentialSubject().put("gx:providedBy", providedBy);
        request.getCredentialSubject().put(ID, id);
        request.getCredentialSubject().put("gx:name", request.getName());
        if (request.getDescription() != null) {
            request.getCredentialSubject().put("gx:description", request.getDescription());
        }
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        VerifiableCredential verifiableCredential = VerifiableCredential.builder()
                .serviceOffering(VerifiableCredential.ServiceOffering.builder()
                        .context(this.contextConfig.serviceOffer())
                        .type(StringPool.VERIFIABLE_CREDENTIAL)
                        .id(id)
                        .issuer(participant.getDid())
                        .issuanceDate(issuanceDate)
                        .credentialSubject(request.getCredentialSubject())
                        .build()).build();
        List<VerifiableCredential> verifiableCredentialList = new ArrayList<>();
        verifiableCredentialList.add(verifiableCredential);
        String privateKey = participant.getId().toString();
        if (!participant.isKeyStored()) {
            privateKey = HashingService.encodeToBase64(request.getPrivateKey());
        }
        SignerServiceRequest signerServiceRequest = new SignerServiceRequest(participant.getDid(), request.getVerificationMethod(), privateKey, verifiableCredential, participant.isKeyStored());
        try {
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceVc = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get(DATA)).get(COMPLETE_SD));
            String trustIndex = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get(DATA)).get(TRUST_INDEX));
            response.put(SERVICE_VC, serviceVc);
            if (trustIndex != null) {
                response.put(TRUST_INDEX, trustIndex);
            }
            if (serviceVc != null) {
                this.hostJsonFile(serviceVc, participant.getId(), name);
            }
            log.debug("Send request to signer for service create vc");
            return response;
        } catch (Exception e) {
            log.debug("Error while signing service offer VC. ", e.getMessage());
            throw new SignerException(e);
        }
    }

    public String signResource(Map<String, Object> resourceRequest, UUID id, String name) {
        try {
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.signResource(resourceRequest);
            String signResource = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get(DATA)).get(COMPLETE_SD));
            if (signResource != null) {
                this.hostJsonFile(signResource, id, name);
            }
            return signResource;
        } catch (Exception e) {
            log.debug("Error while signing resource VC. ", e.getMessage());
            throw new SignerException(e.getMessage());
        }
    }

    private void hostJsonFile(String hostServiceOfferJson, UUID id, String name) {
        File file = new File(TEMP_FOLDER + name + JSON_EXTENSION);
        try {
            FileUtils.writeStringToFile(file, hostServiceOfferJson, Charset.defaultCharset());
            String hostedPath = id + "/" + name + JSON_EXTENSION;
            this.s3Utils.uploadFile(hostedPath, file);
        } catch (Exception e) {
            log.error("Error while hosting service offer json for participant: {}", id, e.getMessage());
            throw new BadDataException(e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }


    public String signLabelLevel(Map<String, Object> labelLevelRequest, UUID id, String name) {
        try {
            ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.signLabelLevel(labelLevelRequest);
            String signResource = this.mapper.writeValueAsString(((Map<String, Object>) Objects.requireNonNull(signerResponse.getBody()).get(DATA)).get("selfDescriptionCredential"));
            if (signResource != null) {
                this.hostJsonFile(signResource, id, name);
            }
            return signResource;
        } catch (BadDataException be) {
            log.debug("Bad Data Exception while signing label level VC. {}", be.getMessage());
            throw new BadDataException(be.getMessage());
        } catch (ConflictException be) {
            log.debug("Conflict Exception while signing label level VC. {}", be.getMessage());
            throw new ConflictException(be.getMessage());
        } catch (Exception e) {
            log.debug("Error while signing label level VC. {}", e.getMessage());
            throw new SignerException(e.getMessage());
        }
    }

    public void validateRequestUrl(List<String> urls, List<String> gxTypeList, String urlTypeLabel, String message, List<String> policy) {
        AtomicReference<ParticipantVerifyRequest> participantValidatorRequest = new AtomicReference<>();
        if (policy == null) {
            policy = this.policies;
        }

        boolean checkType = !CollectionUtils.isEmpty(gxTypeList);
        List<String> finalPolicy = policy;
        urls.parallelStream().forEach(url -> {
            ResponseEntity<JsonNode> signerResponse;
            try {
                participantValidatorRequest.set(new ParticipantVerifyRequest(url, finalPolicy));
                signerResponse = this.signerClient.verify(participantValidatorRequest.get());
                log.debug("signer validation response: {}", Objects.requireNonNull(signerResponse.getBody()).get("message").asText());
            } catch (Exception e) {
                log.error("An error occurred for URL:{}, policies: {}", url, finalPolicy, e);
                throw new BadDataException(this.messageSource.getMessage(message, null, LocaleContextHolder.getLocale()) + " URL=" + url);
            }

            if (checkType && !gxTypeList.contains(signerResponse.getBody().get(DATA).get(VERIFY_URL_TYPE).asText())) {
                String urlType = gxTypeList.size() == 1 ? gxTypeList.get(0) : "resource";
                String messageKey = StringUtils.hasText(urlTypeLabel) ? "invalid.url.type.with.label" : "invalid.url.type";
                throw new BadDataException(this.messageSource.getMessage(messageKey, new String[]{urlType, urlTypeLabel}, LocaleContextHolder.getLocale()));
            }
        });
    }

    public void addServiceEndpoint(UUID participantId, String id, String type, String url) {
        Map<String, String> map = Map.of(ID, id, TYPE, type, "serviceEndpoints", url);
        String didPath = TEMP_FOLDER + UUID.randomUUID() + JSON_EXTENSION;
        File file = null;
        File updatedFile = new File(TEMP_FOLDER + UUID.randomUUID() + JSON_EXTENSION);
        try {
            file = this.s3Utils.getObject(participantId + "/" + DID_JSON, didPath);
            String didString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(didString);
            JSONArray services = jsonObject.optJSONArray(SERVICE);
            if (Objects.isNull(services)) {
                jsonObject.put(SERVICE, new ArrayList<>());
                services = jsonObject.getJSONArray(SERVICE);
            }
            List<String> serviceIds = new ArrayList<>();
            for (Object service : services) {
                JSONObject s = (JSONObject) service;
                serviceIds.add(s.getString(ID));
            }
            if (!serviceIds.contains(id)) {
                services.put(map);
            }
            FileUtils.writeStringToFile(updatedFile, jsonObject.toString(), Charset.defaultCharset());
            this.s3Utils.uploadFile(participantId + "/" + DID_JSON, updatedFile);
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
            return (boolean) ((Map<String, Object>) Objects.requireNonNull(response.getBody()).get(DATA)).get(IS_VALID);
        } catch (Exception ex) {
            log.error("Issue occurred while validating did {} with verification method {}", issuerDid, verificationMethod);
            return false;
        }
    }

    public boolean validateRegistrationNumber(Map<String, Object> credential) {
        try {
            Map<String, Object> request = new HashMap<>();
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            Map<String, Object> legalRegistrationNumber = this.mapper.convertValue(credential.get(LEGAL_REGISTRATION_NUMBER), typeReference);
            legalRegistrationNumber.put(CONTEXT, this.contextConfig.registrationNumber());
            legalRegistrationNumber.put(TYPE, GX_LEGAL_REGISTRATION_NUMBER);
            legalRegistrationNumber.put(ID, GAIA_X_LEGAL_REGISTRATION_NUMBER_DID);
            request.put(LEGAL_REGISTRATION_NUMBER, legalRegistrationNumber);
            ResponseEntity<Map<String, Object>> response = this.signerClient.validateRegistrationNumber(request);
            return (boolean) ((Map<String, Object>) Objects.requireNonNull(response.getBody()).get(DATA)).get(IS_VALID);
        } catch (Exception ex) {
            log.error("Issue occurred while validating the registration number.", ex);
            return false;
        }
    }

    private void createDidCreationJob(Participant participant) {
        try {
            this.scheduleService.createJob(participant.getId().toString(), StringPool.JOB_TYPE_CREATE_DID, 0);
            log.info("K8sService(createDidCreationJob) -> DID creation cron has been scheduled.");
        } catch (SchedulerException e) {
            log.info("K8sService(createDidCreationJob) -> DID creation failed for participant {}", participant.getId());
            participant.setStatus(RegistrationStatus.DID_JSON_CREATION_FAILED.getStatus());
        }
    }
}
