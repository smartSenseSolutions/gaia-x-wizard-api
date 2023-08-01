/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.signer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.model.CreateDidRequest;
import eu.gaiax.wizard.api.model.CreateVCRequest;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
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
    private final ContextConfig contextConfig;


    public void createParticipantJson(Participant participant, String key, boolean ownDid) {
        File file = new File("/tmp/participant.json");
        try {
            String privateKey = key;
            if (ownDid) {
                privateKey = (String) this.vault.get(key).get("key");
            }
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            Map<String, Object> credentials = this.objectMapper.convertValue(participant.getCredential(), typeReference);
            //TODO need to verify for the verification method
            CreateVCRequest request = new CreateVCRequest(HashingService.encodeToBase64(privateKey), participant.getDid(), participant.getDid(), credentials);
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createVc(request);
            String participantString = this.objectMapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get("data")).get("verifiableCredential"));
            FileUtils.writeStringToFile(file, participantString, Charset.defaultCharset());
            String hostedPath = participant.getDid() + "/participant.json";
            this.s3Utils.uploadFile(hostedPath, file);
            this.credentialService.createCredential(participantString, hostedPath, "Participant", null, participant);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATED.getStatus());
            log.debug("participant json created for enterprise->{} , json ->{}", participant.getId(), participantString);
        } catch (Exception e) {
            log.error("Error while creating participant json for enterprise -{}", participant.getId(), e);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
            CommonUtils.deleteFile(file);
        }
    }

    public void createDid(Participant participant) {
        File file = new File("/tmp/did.json");
        try {
            String domain = participant.getDomain();
            CreateDidRequest createDidRequest = new CreateDidRequest(domain);
            ResponseEntity<Map<String, Object>> responseEntity = this.signerClient.createDid(createDidRequest);
            String didString = this.objectMapper.writeValueAsString(((Map<String, Object>) responseEntity.getBody().get("data")).get("did"));
            FileUtils.writeStringToFile(file, didString, Charset.defaultCharset());
            this.s3Utils.uploadFile(participant.getDid() + "/did.json", file);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATED.getStatus());
            log.debug("Did created for enterprise->{} , did ->{}", participant.getDid(), didString);
            this.createParticipantCreationJob(participant);
        } catch (Exception e) {
            log.error("Error while creating did json for enterprise -{}", participant.getDid(), e);
            participant.setStatus(RegistrationStatus.DID_JSON_CREATION_FAILED.getStatus());
        } finally {
            this.participantRepository.save(participant);
            CommonUtils.deleteFile(file);
        }
    }

    private void createParticipantCreationJob(Participant participant) {
        try {
            this.scheduleService.createJob(participant.getDid(), StringPool.JOB_TYPE_CREATE_PARTICIPANT, 0);
            participant.setStatus(RegistrationStatus.PARTICIPANT_JSON_CREATION_FAILED.getStatus());
        } catch (Exception e) {
            log.error("Can not create participant job for enterprise -{}", participant.getDid(), e);
        }
    }

}
