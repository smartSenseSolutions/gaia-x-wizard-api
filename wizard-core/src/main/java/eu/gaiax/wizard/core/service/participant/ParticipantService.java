package eu.gaiax.wizard.core.service.participant;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.exception.ParticipantNotFoundException;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.data_master.EntityTypeMasterRepository;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EntityTypeMasterRepository entityTypeMasterRepository;
    private final SignerService signerService;
    private final DomainService domainService;
    private final K8SService k8SService;
    private final CertificateService certificateService;
    private final CredentialService credentialService;
    private final ObjectMapper mapper;

    //TODO need to finalize the onboarding request from frontend team
    @SneakyThrows
    public void onboardParticipant(ParticipantOnboardRequest request, String email) {
        EntityTypeMaster entityType = this.entityTypeMasterRepository.findById(UUID.fromString(request.entityType())).orElse(null);
        Validate.isNull(entityType).launch("invalid.entity.type");
        this.validateOnboardRequest(request);
        Participant participant = this.participantRepository.getByEmail(email);
        Validate.isNull(participant).launch(new ParticipantNotFoundException("participant.not.found"));
        participant.setCredential(this.mapper.writeValueAsString(request.credential()));
        participant.setDid(request.issuerDid());
        participant.setLegalName(request.legalName());
        participant.setShortName(request.shortName());
        this.participantRepository.save(participant);
        //TODO Generate VC for Registration number
        //TODO create VC for T&C
        //TODO create participant Json
        //TODO need to update the vault path for private key
        //TODO need to save the json on credential table
        //Below method call will schedule the job for upcoming operation.
        //If user having own did solution then for those user we will create participant json directly else
        //we create the Subdomain, certificate, ingress and DID and participant json.
        if (request.ownDid()) {
            this.participantWithDidSolution(participant, request);
        } else {
            this.participantWithoutDidSolution(participant);
        }
    }

    private void participantWithDidSolution(Participant participant, ParticipantOnboardRequest request) {
        //TODO need to pass PK directly
        this.signerService.createParticipantJson(participant, request.credential(), participant.getDomain(), null);
    }

    private void validateOnboardRequest(ParticipantOnboardRequest request) {
        Validate.isFalse(StringUtils.hasText(request.legalName())).launch("invalid.legal.name");
        Validate.isFalse(StringUtils.hasText(request.shortName())).launch("invalid.short.name");
        Validate.isFalse(CollectionUtils.isEmpty(request.credential())).launch("invalid.credential");
        if (request.ownDid()) {
            Validate.isFalse(StringUtils.hasText(request.issuerDid())).launch("invalid.did");
            Validate.isFalse(StringUtils.hasText(request.privateKey())).launch("invalid.private.key");
            Validate.isFalse(StringUtils.hasText(request.verificationMethod())).launch("invalid.verification.method");
            Validate.isTrue(this.validateDidWithPrivateKey(request.issuerDid(), request.verificationMethod(), request.privateKey())).launch("invalid.did.or.private.key");
        }
    }

    //TODO need to resolve DID and validate the private key from given verification method
    private boolean validateDidWithPrivateKey(String did, String verificationMethod, String privateKey) {
        return true;
    }

    private void participantWithoutDidSolution(Participant participant) {
        //Here Quartz will manage the further flow with JobBean.
        //Quartz schedule for the ssl certificate, ingress and did creation process.
        this.domainService.createSubDomain(participant);
    }

    @SneakyThrows
    public void validateParticipant(ParticipantValidatorRequest request) {
        //TODO need to confirm the endpoint from Signer tool which will validate the participant json. Work will start from monday.
        //TODO assume that we got the did  from signer tool
        final String did = "did";
        Participant participant = this.participantRepository.getByDid(did);
        if (Objects.isNull(participant)) {
            participant = Participant.builder()
                    .did(did)
                    .build();
        }
        participant = this.participantRepository.save(participant);
        String participantJson = InvokeService.executeRequest(request.participantJsonUrl(), HttpMethod.GET);
        this.credentialService.createCredential(participantJson, request.participantJsonUrl(), "Legal Participant", null, participant);
        if (request.store()) {
            this.certificateService.uploadCertificatesToVault(participant.getDid(), null, null, request.privateKey(), null);
        }
    }

}
