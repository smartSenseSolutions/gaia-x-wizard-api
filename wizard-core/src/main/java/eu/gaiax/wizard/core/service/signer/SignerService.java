/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.signer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.*;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

/**
 * The type Signer service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignerService {

    private final CredentialService credentialService;
    private final ParticipantRepository participantRepository;
    private final SignerClient signerClient;
    private final S3Utils s3Utils;
    private final ObjectMapper objectMapper;
    private final ScheduleService scheduleService;
    private final Vault vault;

    public void createParticipantJson(UUID participantId) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}", participantId);
        Participant participant = this.participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        this.createParticipantJson(participant, participant.getId().toString(), participant.isOwnDidSolution());
    }

    public void createParticipantJson(Participant participant, String key, boolean ownDid) {
        log.info("SignerService(createParticipantJson) -> Initiate the legal participate creation process for participant {}, ownDid {}", participant.getId(), ownDid);
        File file = new File("/tmp/participant.json");
        try {
            String privateKey = key;
            if (!ownDid) {
                log.info("SignerService(createParticipantJson) -> PrivateKey(pkcs8.key) needs to resolve from vault with key {}", key);
                privateKey = (String) this.vault.get(key).get("pkcs8.key");
            }
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            Map<String, Object> credentials = this.objectMapper.readValue(participant.getCredential(), typeReference);
            CreateVCRequest request = new CreateVCRequest(HashingService.encodeToBase64(privateKey), participant.getDid(), participant.getDid(), credentials);
            log.info("SignerService(createParticipantJson) -> Initiate the signer client call to create legal participant json.");
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createVc(request);
            log.info("SignerService(createParticipantJson) -> Receive success response from signer tool.");
            String participantString = this.objectMapper.writeValueAsString(responseEntity.getBody().get("data"));
            FileUtils.writeStringToFile(file, participantString, Charset.defaultCharset());
            String hostedPath = participant.getId() + "/participant.json";
            this.s3Utils.uploadFile(hostedPath, file);
            this.credentialService.createCredential(participantString, hostedPath, CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType(), null, participant);
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
        File file = new File("/tmp/did.json");
        Participant participant = this.participantRepository.findById(participantId).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        try {
            String domain = participant.getDomain();
            log.info("SignerService(createDid) ->  DID creation is initiated for domain {}", domain);
            CreateDidRequest createDidRequest = new CreateDidRequest(domain);
            log.info("SignerService(createDid) -> Initiated signerClient call for create did for domain {}", domain);
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createDid(createDidRequest);
            log.info("SignerService(createDid): -> Response has been received from signerClient for domain {}", domain);
            String didString = this.objectMapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get("data")).get("did"));
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
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
            log.info("SignerService(createParticipantCreationJob) -> Create legal participant corn has been scheduled.");
        } catch (Exception e) {
            log.error("SignerService(createParticipantCreationJob) -> Not able to create legal participant corn for participant {}", participant.getId(), e);
        }
    }

}
