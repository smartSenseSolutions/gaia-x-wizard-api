package eu.gaiax.wizard.core.service.participant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.ParticipantVerifyRequest;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.keycloak.KeycloakService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantCreationRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantRegisterRequest;
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
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipantService extends BaseService<Participant, UUID> {

    @Value("${wizard.domain}")
    private String domain;
    @Value("${wizard.host.wizard}")
    private String wizardHost;
    @Value("${wizard.gaiax.tnc}")
    private String tnc;
    private final ContextConfig contextConfig;
    private final ParticipantRepository participantRepository;
    private final EntityTypeMasterRepository entityTypeMasterRepository;
    private final SignerService signerService;
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final CredentialService credentialService;
    private final KeycloakService keycloakService;
    private final S3Utils s3Utils;
    private final Vault vault;
    private final ObjectMapper mapper;
    private final SpecificationUtil<Participant> specificationUtil;
    private final SignerClient signerClient;
    private static final List<String> policies = Arrays.asList(
            "integrityCheck",
            "holderSignature",
            "complianceSignature",
            "complianceCheck"
    );

    @Transactional
    @SneakyThrows
    public Participant registerParticipant(ParticipantRegisterRequest request) {
        log.debug("ParticipantService(registerParticipant) -> Participant registertation with email {}", request.email());
        Validate.isFalse(StringUtils.hasText(request.email())).launch("email.required");
        ParticipantOnboardRequest onboardRequest = request.onboardRequest();
        this.validateParticipantOnboardRequest(onboardRequest);
        EntityTypeMaster entityType = this.entityTypeMasterRepository.findById(UUID.fromString(onboardRequest.entityType())).orElse(null);
        Validate.isNull(entityType).launch("invalid.entity.type");
        Participant participant = this.participantRepository.getByEmail(request.email());
        Validate.isNotNull(participant).launch("participant.already.registered");
        Validate.isNotNull(this.participantRepository.getByLegalName(request.onboardRequest().legalName())).launch("legal.name.already.registered");
        participant = this.create(Participant.builder()
                .email(request.email())
                .did(onboardRequest.issuerDid())
                .legalName(onboardRequest.legalName())
                .shortName(onboardRequest.shortName())
                .entityType(entityType)
                .domain(onboardRequest.ownDid() ? null : onboardRequest.shortName() + "." + this.domain)
                .participantType("REGISTERED")
                .credential(this.mapper.writeValueAsString(onboardRequest.credential()))
                .ownDidSolution(onboardRequest.ownDid())
                .build());
        this.keycloakService.createParticipantUser(participant.getId().toString(), participant.getLegalName(), participant.getEmail());
        this.keycloakService.sendRequiredActionsEmail(participant.getEmail());
        return participant;
    }

    private void validateParticipantOnboardRequest(ParticipantOnboardRequest request) {
        Validate.isFalse(StringUtils.hasText(request.legalName())).launch("invalid.legal.name");
        Validate.isFalse(StringUtils.hasText(request.shortName())).launch("invalid.short.name");
        Validate.isTrue(CollectionUtils.isEmpty(request.credential())).launch("invalid.credential");
        Map<String, Object> credential = request.credential();
        Object legalParticipant = credential.get("legalParticipant");
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Object credentialSubject = this.mapper.convertValue(legalParticipant, typeReference).get("credentialSubject");
        this.validateOnboardedCredentialSubject(credentialSubject);
    }

    public Participant initiateOnboardParticipantProcess(String participantId, ParticipantCreationRequest request) {
        log.debug("ParticipantService(initiateOnboardParticipantProcess) -> Prepare legal participant json with participant {}", participantId);
        Participant participant = this.participantRepository.findById(UUID.fromString(participantId)).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        if (participant.isOwnDidSolution()) {
            log.debug("ParticipantService(initiateOnboardParticipantProcess) -> Validate participant {} has own did solution.", participantId);
            Validate.isFalse(StringUtils.hasText(request.issuer())).launch("invalid.did");
            Validate.isFalse(StringUtils.hasText(request.privateKey())).launch("invalid.private.key");
            Validate.isFalse(StringUtils.hasText(request.verificationMethod())).launch("invalid.verification.method");
            Validate.isTrue(this.validateDidWithPrivateKey(request.issuer(), request.verificationMethod(), request.privateKey())).launch("invalid.did.or.private.key");
        }
        Credential credentials = this.credentialService.getLegalParticipantCredential(participant.getId());
        Validate.isNotNull(credentials).launch("already.legal.participant");
        this.prepareCredentialSubjectForLegalParticipant(participant);
        this.createLegalParticipantJson(participant, request.privateKey());
        if (request.store()) {
            this.certificateService.uploadCertificatesToVault(participantId, participantId, null, null, null, request.privateKey());
        }
        return participant;
    }

    private void createLegalParticipantJson(Participant participant, String privateKey) {
        if (participant.isOwnDidSolution()) {
            log.debug("ParticipantService(createLegalParticipantJson) -> Create Legal participant {} who has own did solutions.", participant.getId());
            this.createLegalParticipantWithDidSolutions(participant, privateKey);
        } else {
            log.debug("ParticipantService(createLegalParticipantJson) -> Create Legal participant {} who don't have own did solutions.", participant.getId());
            this.createLegalParticipantWithoutDidSolutions(participant);
        }
    }

    private void createLegalParticipantWithDidSolutions(Participant participant, String privateKey) {
        log.debug("ParticipantService(createLegalParticipantJson) -> Create participant json.");
        this.signerService.createParticipantJson(participant, privateKey, true);
    }

    private void createLegalParticipantWithoutDidSolutions(Participant participant) {
        log.debug("ParticipantService(createLegalParticipantJson) -> Create Subdomain, Certificate, Ingress, Did and participant json.");
        this.domainService.createSubDomain(participant.getId());
    }

    @SneakyThrows
    private Map<String, Object> prepareCredentialSubjectForLegalParticipant(Participant participant) {
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> Prepare credential subject for signer tool.");
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Map<String, Object> credential = this.mapper.readValue(participant.getCredential(), typeReference);
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
        participantCredentialSubject.put("id", this.formParticipantJsonUrl(participant.getId()) + "#0");
        participantCredentialSubject.put("type", "gx:LegalParticipant");
        String registrationId = this.formParticipantJsonUrl(participant.getId()) + "#1";
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
        tncCredentialSubject.put("id", this.formParticipantJsonUrl(participant.getId()) + "#2");
        tncCredentialSubject.put("gx:termsAndConditions", this.tnc.replaceAll("\\\\n", "\n"));

        tncVc.put("credentialSubject", tncCredentialSubject);

        credential.put("legalParticipant", legalParticipant);
        credential.put("legalRegistrationNumber", legalRegistrationNumber);
        credential.put("gaiaXTermsAndConditions", tncVc);
        participant.setCredential(this.mapper.writeValueAsString(credential));
        this.create(participant);
        log.info("ParticipantService(prepareCredentialSubjectForLegalParticipant) -> CredentialSubject has been created successfully.");
        return credential;
    }

    private String formParticipantJsonUrl(UUID participantId) {
        return this.wizardHost + participantId.toString() + "/participant.json";
    }

    private void validateOnboardedCredentialSubject(Object credentialSubject) {
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        Map<String, Object> credentials = this.mapper.convertValue(credentialSubject, typeReference);
        String legalName = (String) credentials.get("gx:legalName");
        Validate.isFalse(StringUtils.hasText(legalName)).launch("invalid.legal.name");
        Object legalAddress = credentials.get("gx:legalAddress");
        Object headquarterAddress = credentials.get("gx:headquarterAddress");
        String legalCountry = (String) this.mapper.convertValue(legalAddress, typeReference).get("gx:countrySubdivisionCode");
        String headQuarterCountry = (String) this.mapper.convertValue(headquarterAddress, typeReference).get("gx:countrySubdivisionCode");
        Validate.isFalse(StringUtils.hasText(legalCountry)).launch("invalid.legal.address");
        Validate.isFalse(StringUtils.hasText(headQuarterCountry)).launch("invalid.headquarter.address");
    }

    //TODO need to resolve DID and validate the private key from given verification method
    private boolean validateDidWithPrivateKey(String did, String verificationMethod, String privateKey) {
        return true;
    }

    @SneakyThrows
    public Participant validateParticipant(ParticipantValidatorRequest request) {
        //TODO need to confirm the endpoint from Signer tool which will validate the participant json. Work will start from monday.
        //TODO assume that we got the did  from signer tool
        ParticipantVerifyRequest participantValidatorRequest = new ParticipantVerifyRequest(request.participantJsonUrl(), policies);
        ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.verify(participantValidatorRequest);
        if (!signerResponse.getStatusCode().is2xxSuccessful()) {
            throw new BadDataException();
        }
        Participant participant = this.participantRepository.getByDid(request.issuer());
        if (Objects.isNull(participant)) {
            participant = Participant.builder()
                    .did(request.issuer())
                    .build();
        }
        participant = this.participantRepository.save(participant);
        String participantJson = InvokeService.executeRequest(request.participantJsonUrl(), HttpMethod.GET);
        Credential credential = this.credentialService.getLegalParticipantCredential(participant.getId());
        if (Objects.isNull(credential)) {
            credential = this.credentialService.createCredential(participantJson, request.participantJsonUrl(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType(), null, participant);
        }
        if (request.store()) {
            this.certificateService.uploadCertificatesToVault(participant.getDomain(), participant.getId().toString(), null, null, null, request.privateKey());
        }
        return participant;
    }


    public String getWellKnownFiles(String host, String fileName) throws IOException {
        log.info("ParticipantService(getWellKnownFiles) -> Fetch wellKnown file for host {} and filename {}", host, fileName);
        Validate.isTrue(fileName.endsWith("key") || fileName.endsWith("csr")).launch(new EntityNotFoundException("Can find file -> " + fileName));
        Participant participant = this.participantRepository.getByDomain(host);
        Validate.isNull(participant).launch(new EntityNotFoundException("subdomain.not.found"));
        if (fileName.equals("did.json")) {
            return this.getLegalParticipantJson(participant.getId().toString(), fileName);
        }
        Map<String, Object> certificates = this.vault.get(participant.getId().toString());
        Object certificate = certificates.get(fileName);
        Validate.isNull(certificate).launch(new EntityNotFoundException("certificate.not.found"));
        return (String) certificate;
    }

    public String getLegalParticipantJson(String participantId, String filename) throws IOException {
        log.info("ParticipantService(getParticipantFile) -> Fetch files from s3 bucket with Id {} and filename {}", participantId, filename);
        Participant participant = this.participantRepository.findById(UUID.fromString(participantId)).orElse(null);
        Validate.isNull(participant).launch(new EntityNotFoundException("participant.not.found"));
        File file = this.s3Utils.getObject(participantId + "/" + filename, filename);
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }

    public Map<String, Object> checkIfParticipantRegistered(String email) {
        return Map.of(StringPool.USER_REGISTERED, this.keycloakService.getKeycloakUserByEmail(email) != null);
    }

    public Participant changeStatus(UUID participantId, int status) {
        Participant participant = this.participantRepository.findById(participantId).orElseThrow(EntityNotFoundException::new);
        participant.setStatus(status);
        return this.create(participant);
    }

    @Override
    protected BaseRepository<Participant, UUID> getRepository() {
        return this.participantRepository;
    }

    @Override
    protected SpecificationUtil<Participant> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
