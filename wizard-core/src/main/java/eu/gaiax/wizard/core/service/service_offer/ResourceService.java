package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository repository;
    private final ParticipantService participantService;
    private final ParticipantRepository participantRepository;
    private final ContextConfig contextConfig;
    private final ObjectMapper objectMapper;
    private final S3Utils s3Utils;
    private final CredentialService credentialService;
    @Value("${wizard.host.wizard}")
    private String wizardHost;

    public Resource createResource(CreateResourceRequest request, String email) throws JsonProcessingException {
        Participant participant;
        if (StringUtils.hasText(email)) {
            participant = this.participantRepository.getByEmail(email);
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.validation().get("participantJson").toString(), request.validation().get("verificationMethod").toString(), request.validation().get("privateKey").toString(), (boolean) request.validation().get("vault"));
            participant = this.participantService.validateParticipant(participantValidatorRequest);
        }
        this.validateResourceRequest(request);
        String json = this.resourceVc(request, participant);
        if (StringUtils.hasText(json)) {
            String hostUrl = participant.getId() + "/" + "resource_" + UUID.randomUUID() + ".json";
            this.hostResourceJson(json, hostUrl);
            Credential resourceVc = this.credentialService.createCredential(json, this.wizardHost + hostUrl, CredentialTypeEnum.RESOURCE.getCredentialType(), "", participant);
            Resource resource = Resource.builder().name(request.credentialSubject().get("gx:name").toString())
                    .credential(resourceVc)
                    .type(request.credentialSubject().get("@type").toString())
                    .subType(request.credentialSubject().get("@subType") == null ? null : request.credentialSubject().get("@subType").toString())
                    .description(request.credentialSubject().get("gx:description") == null ? null : request.credentialSubject().get("gx:description").toString())
                    .participant(participant).build();
            return this.repository.save(resource);
        }
        return null;
    }

    private void validateResourceRequest(CreateResourceRequest request) {
        Validate.isFalse(StringUtils.hasText(request.credentialSubject().get("gx:name").toString())).launch("invalid.resource.name");
    }

    public String resourceVc(CreateResourceRequest request, Participant participant) throws JsonProcessingException {
        String issuanceDate = LocalDateTime.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Map<String, Object> resourceRequest = new HashMap<>();
        resourceRequest.put("@context", this.contextConfig.resource());
        resourceRequest.put("type", Collections.singleton("VerifiableCredential"));
        resourceRequest.put("id", this.wizardHost + "resource" + "/" + UUID.randomUUID() + ".json");
        resourceRequest.put("issuer", "did:web:dev.smartproof.in");
        resourceRequest.put("issuanceDate", issuanceDate);
        Map<String, Object> credentialSubject = request.credentialSubject();
        if (credentialSubject != null) {
            credentialSubject.put("@context", this.contextConfig.resource());
            credentialSubject.put("@id", this.wizardHost + participant.getId() + "/" + "resource_" + UUID.randomUUID() + ".json");
            credentialSubject.put("@type", "gx:" + request.credentialSubject().get("@type").toString());
        }
        resourceRequest.put("credentialSubject", credentialSubject);


        //Todo singer code remaining

        String resourceJson = this.objectMapper.writeValueAsString(resourceRequest);
        return resourceJson;
    }

    public void hostResourceJson(String resourceJson, String hostedPath) {
        File file = new File("/tmp/" + hostedPath);
        try {
            FileUtils.writeStringToFile(file, resourceJson, Charset.defaultCharset());
            this.s3Utils.uploadFile(hostedPath, file);
        } catch (Exception e) {
            log.error("Error while hosting service offer json for participant:{},error:{}", hostedPath, e.getMessage());
        } finally {
            CommonUtils.deleteFile(file);
        }
    }
}
