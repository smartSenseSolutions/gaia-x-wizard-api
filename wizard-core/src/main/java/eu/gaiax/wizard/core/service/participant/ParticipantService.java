package eu.gaiax.wizard.core.service.participant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.exception.ParticipantNotFoundException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.keycloak.KeycloakService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.data_master.EntityTypeMasterRepository;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipantService {

    private final ContextConfig contextConfig;
    private final ParticipantRepository participantRepository;
    private final EntityTypeMasterRepository entityTypeMasterRepository;
    private final SignerService signerService;
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final CredentialService credentialService;
    private final Vault vault;
    private final ObjectMapper mapper;
    private final KeycloakService keycloakService;

    //TODO need to finalize the onboarding request from frontend team
    @SneakyThrows
    public void onboardParticipant(ParticipantOnboardRequest request, String email) {
        EntityTypeMaster entityType = this.entityTypeMasterRepository.findById(UUID.fromString(request.entityType())).orElse(null);
        Validate.isNull(entityType).launch("invalid.entity.type");
        this.validateOnboardRequest(request);
        Participant participant = this.participantRepository.getByEmail(email);
        //TODO need to remove particpant creation flow
        if (Objects.isNull(participant)) {
            participant = this.participantRepository.save(Participant.builder()
                    .ownDidSolution(false).shortName(request.shortName())
                    .legalName(request.legalName()).did("did:web:hella.proofsense.in").email(email).build());
        }

        Validate.isNull(participant).launch(new ParticipantNotFoundException("participant.not.found"));
        //TODO needs to validate participant details
        Credential credentials = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        Validate.isNotNull(credentials).launch("already.legal.participant");

        //TODO need to update credential based on the final request --> https://jsonblob.com/1133307139513925632
        //TODO add context in credential
        //TODO add type in credential
        participant.setDid("did:web:hella.proofsense.in");
        participant.setLegalName(request.legalName());
        participant.setShortName(request.shortName());
        participant.setDomain(request.shortName() + ".smart-x.smartsenselabs.com");
        participant.setOwnDidSolution(request.ownDid());
        this.prepareCredential(request.credential(), participant);
        participant.setCredential(this.mapper.writeValueAsString(request.credential()));
        participant = this.participantRepository.save(participant);
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
        if (request.store()) {
            this.certificateService.uploadCertificatesToVault(participant.getDomain(), participant.getId().toString(), null, null, request.privateKey(), null);
        }
    }

    //TODO this method will add the information about the context,T&C vc, type,id,issuerDate,issuer
    //note id represent holder, issuer represent issuer of vc
    private void prepareCredential(Map<String, Object> credential, Participant participant) {
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
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
        participantCredentialSubject.put("id", "#0");
        participantCredentialSubject.put("type", "gx:LegalParticipant");
        final String registrationId = "#1";
        participantCredentialSubject.put("gx:legalRegistrationNumber", registrationId);

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
        tncCredentialSubject.put("id", "#2");
        tncCredentialSubject.put("gx:termsAndConditions", "The PARTICIPANT signing the Self-Description agrees as follows:\\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\\n\\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).");

        tncVc.put("credentialSubject", tncCredentialSubject);

        credential.put("legalParticipant", legalParticipant);
        credential.put("legalRegistrationNumber", legalRegistrationNumber);
        credential.put("gaiaXTermsAndConditions", tncVc);
    }

    private void participantWithDidSolution(Participant participant, ParticipantOnboardRequest request) {
        //TODO need to pass PK directly
        this.signerService.createParticipantJson(participant, request.privateKey(), request.ownDid());
    }

    private void validateOnboardRequest(ParticipantOnboardRequest request) {
        Validate.isFalse(StringUtils.hasText(request.legalName())).launch("invalid.legal.name");
        Validate.isFalse(StringUtils.hasText(request.shortName())).launch("invalid.short.name");
        Validate.isTrue(CollectionUtils.isEmpty(request.credential())).launch("invalid.credential");
        //TODO need to validate the credential with the compulsory fields
        Map<String, Object> credential = request.credential();
        Object legalParticipant = credential.get("legalParticipant");
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Object credentialSubject = this.mapper.convertValue(legalParticipant, typeReference).get("credentialSubject");
        this.validateCredentialSubject(credentialSubject);
        if (request.ownDid()) {
            Validate.isFalse(StringUtils.hasText(request.issuerDid())).launch("invalid.did");
            Validate.isFalse(StringUtils.hasText(request.privateKey())).launch("invalid.private.key");
            Validate.isFalse(StringUtils.hasText(request.verificationMethod())).launch("invalid.verification.method");
            Validate.isTrue(this.validateDidWithPrivateKey(request.issuerDid(), request.verificationMethod(), request.privateKey())).launch("invalid.did.or.private.key");
        }
    }

    private void validateCredentialSubject(Object credentialSubject) {
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Object legalAddress = this.mapper.convertValue(credentialSubject, typeReference).get("gx:legalAddress");
        Object headquarterAddress = this.mapper.convertValue(credentialSubject, typeReference).get("gx:headquarterAddress");
        String legalCountry = (String) this.mapper.convertValue(legalAddress, typeReference).get("gx:countrySubdivisionCode");
        String headQuarterCountry = (String) this.mapper.convertValue(headquarterAddress, typeReference).get("gx:countrySubdivisionCode");
        Validate.isFalse(StringUtils.hasText(legalCountry)).launch("invalid.legal.address");
        Validate.isFalse(StringUtils.hasText(headQuarterCountry)).launch("invalid.headquarter.address");
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
        Credential credential = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        if (Objects.isNull(credential)) {
            credential = this.credentialService.createCredential(participantJson, request.participantJsonUrl(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType(), null, participant);
        }
        if (request.store()) {
            this.certificateService.uploadCertificatesToVault(participant.getDomain(), participant.getId().toString(), null, null, request.privateKey(), null);
        }
    }

    public String getParticipantFile(String host, String fileName) {
        log.info("getParticipantFile -> Fetch file from host {} and fileName {}", host, fileName);
        Validate.isTrue(fileName.endsWith("key") || fileName.endsWith("csr")).launch(new EntityNotFoundException("Can find file -> " + fileName));
        Participant participant = this.participantRepository.getByDomain(host);
        Validate.isNull(participant).launch("Can not find subdomain -> " + host);
        Map<String, Object> certificates = this.vault.get(participant.getId().toString());
        Object certificate = certificates.get(fileName);
        Validate.isNull(certificate).launch("Can not find subdomain -> " + host);
        return (String) certificate;
    }

    public Map<String, Object> checkIfParticipantRegistered(String email) {
        return Map.of(StringPool.USER_REGISTERED, this.keycloakService.getKeycloakUserByEmail(email) != null);
    }
}
