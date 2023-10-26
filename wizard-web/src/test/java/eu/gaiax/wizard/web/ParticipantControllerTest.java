package eu.gaiax.wizard.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.model.request.ParticipantCreationRequest;
import eu.gaiax.wizard.api.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.api.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.controller.ParticipantController;
import eu.gaiax.wizard.core.service.InvokeService;
import eu.gaiax.wizard.core.service.data_master.EntityTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.participant.VaultService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.CredentialRepository;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import eu.gaiax.wizard.util.HelperService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.api.utils.StringPool.*;
import static eu.gaiax.wizard.util.constant.TestConstant.*;
import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpMethod.POST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {GaiaXWizardApplication.class})
@ActiveProfiles("test")
@ContextConfiguration(initializers = {ContainerContextInitializer.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParticipantControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private EntityTypeMasterService entityTypeMasterService;
    @Autowired
    private SubdivisionCodeMasterService subdivisionCodeMasterService;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    @Autowired
    private SignerClient signerClient;
    @Autowired
    private ParticipantController participantController;
    @Autowired
    @SpyBean
    private CredentialRepository credentialRepository;
    @Autowired
    private MessageSource messageSource;
    @MockBean
    @Autowired
    private VaultService vaultService;
    @MockBean
    @Autowired
    private DomainService domainService;
    
    @BeforeEach
    @BeforeTransaction
    public final void setUp() {
        try {
            this.credentialRepository.deleteAll();
            this.participantRepository.deleteAll();
        } catch (Exception ignored) {
        
        }
    }
    
    private final String randomUUID = UUID.randomUUID().toString();
    
    @Test
    void register_participant_own_did_200() {
        doReturn(HelperService.getValidateRegistrationNumberResponse()).when(this.signerClient).validateRegistrationNumber(anyMap());
        doReturn(HelperService.getVerifyUrlResponseMock(this.mapper)).when(this.signerClient).verify(any());
        
        String entityTypeId = this.entityTypeMasterService.getAll().get(0).getId().toString();
        String subDivision = this.subdivisionCodeMasterService.getAll().get(0).getSubdivisionCode();
        ParticipantRegisterRequest request = HelperService.registerParticipantRequest(LEGAL_NAME, SHORT_NAME, entityTypeId,
                HelperService.prepareDefaultCredential(LEGAL_NAME, subDivision, subDivision),
                true, false, true);
        
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange(REGISTER, POST, new HttpEntity<>(request), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();
        assertEquals(request.email(), payload.get("email"));
        assertEquals(request.onboardRequest().legalName(), payload.get("legalName"));
        assertEquals(request.onboardRequest().shortName(), payload.get("shortName"));
        assertNotNull(payload.get("credentialRequest"));
    }
    
    @Test
    void send_required_actions_email_200() {
        this.register_participant_own_did_200();
        
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange(SEND_REQUIRED_ACTIONS_EMAIL, POST, new HttpEntity<>(new SendRegistrationEmailRequest(EMAIL)), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody().getMessage(), this.messageSource.getMessage("registration.mail.sent", null, Locale.ENGLISH));
    }
    
    @Test
    void check_participant_registered_false() {
        ResponseEntity<CommonResponse> response = this.restTemplate.getForEntity(URI.create(CHECK_REGISTRATION + "?email=" + EMAIL), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();
        
        assertFalse((Boolean) payload.get("userRegistered"));
    }
    
    @Test
    void check_participant_registered_true() {
        this.register_participant_own_did_200();
        
        ResponseEntity<CommonResponse> response = this.restTemplate.getForEntity(URI.create(CHECK_REGISTRATION + "?email=" + EMAIL), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();
        
        assertTrue((Boolean) payload.get("userRegistered"));
        assertFalse((Boolean) payload.get("deviceConfigured"));
    }
    
    @Test
    void initiate_onboarding_participant_own_did_200() {
        this.register_participant_own_did_200();
        
        Participant participant = this.participantRepository.findAll().get(0);
        Map<String, Object> validateDidResponse = new HashMap<>();
        validateDidResponse.put(DATA, Map.of(IS_VALID, true));
        doReturn(ResponseEntity.ok(validateDidResponse)).when(this.signerClient).validateDid(any());
        
        Map<String, Object> vcMap = new HashMap<>();
        vcMap.put(COMPLETE_SD, Map.of(this.randomUUID, this.randomUUID));
        doReturn(ResponseEntity.ok(Map.of(DATA, vcMap))).when(this.signerClient).createVc(any());
        
        ParticipantCreationRequest participantCreationRequest = new ParticipantCreationRequest(true, "did:web:" + this.randomUUID, "did:web:" + this.randomUUID, this.randomUUID, false);
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange(ONBOARD_PARTICIPANT.replace("{participantId}", participant.getId().toString()), POST, new HttpEntity<>(participantCreationRequest), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();
        
        assertEquals(participantCreationRequest.issuer(), payload.get("did"));
        assertEquals(participantCreationRequest.ownDid(), payload.get("ownDidSolution"));
        assertEquals(participantCreationRequest.store(), payload.get("keyStored"));
    }
    
    @Test
    void initiate_onboarding_participant_no_did_200() {
        // mimics unit test behaviour
        this.register_participant_own_did_200();
        
        Participant participant = this.participantRepository.findAll().get(0);
        Map<String, Object> validateDidResponse = new HashMap<>();
        validateDidResponse.put(DATA, Map.of(IS_VALID, true));
        doReturn(ResponseEntity.ok(validateDidResponse)).when(this.signerClient).validateDid(any());
        doNothing().when(this.domainService).createSubDomain(any());
        
        Map<String, Object> vcMap = new HashMap<>();
        vcMap.put(COMPLETE_SD, Map.of(this.randomUUID, this.randomUUID));
        doReturn(ResponseEntity.ok(Map.of(DATA, vcMap))).when(this.signerClient).createVc(any());
        
        ParticipantCreationRequest participantCreationRequest = new ParticipantCreationRequest(false, null, null, null, false);
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange(ONBOARD_PARTICIPANT.replace("{participantId}", participant.getId().toString()), POST, new HttpEntity<>(participantCreationRequest), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void validate_participant_200() {
        doReturn(HelperService.getVerifyUrlResponseMock(this.mapper)).when(this.signerClient).verify(any());
        ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest("http://localhost/" + this.randomUUID, "did:web:" + this.randomUUID, this.randomUUID, false, false);
        
        try (MockedStatic<InvokeService> invokeServiceMockedStatic = Mockito.mockStatic(InvokeService.class)) {
            invokeServiceMockedStatic.when(() -> InvokeService.executeRequest(anyString(), any())).thenReturn(HelperService.generateLegalParticipantMock(this.randomUUID));
            String response = this.participantController.validateParticipant(participantValidatorRequest);
            assertEquals("Success", response);
        }
    }
    
    @Test
    void get_legal_participant_200() throws IOException {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        String legalParticipantJson = this.participantController.getLegalParticipantJson(participant.getId().toString(), "participant.json");
        assertThat(legalParticipantJson).isEqualTo(this.mapper.writeValueAsString(Map.of(this.randomUUID, this.randomUUID)));
    }
    
    @Test
    void get_well_known_file_404() {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        String participantId = participant.getId().toString();
        String domain = participant.getDomain();
        assertThrows(EntityNotFoundException.class, () -> this.participantController.getWellKnownFiles(participantId + ".key", domain));
    }
    
    @Test
    void get_well_known_file_200() throws IOException {
        // mimics unit test behaviour
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        doReturn(Map.of("x509CertificateChain.pem", this.randomUUID)).when(this.vaultService).getParticipantSecretData(anyString());
        
        String wellKnownFile = this.participantController.getWellKnownFiles("x509CertificateChain.pem", participant.getDomain());
        assertThat(wellKnownFile).isEqualTo(this.randomUUID);
    }
    
    @Test
    void get_config_200() {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        JwtAuthenticationToken mockPrincipal = Mockito.mock(JwtAuthenticationToken.class);
        doReturn(Map.of(ID, participant.getId().toString())).when(mockPrincipal).getTokenAttributes();
        
        CommonResponse<ParticipantConfigDTO> participantConfigResponse = this.participantController.getConfig(mockPrincipal);
        assertThat(participantConfigResponse.getPayload().getId()).isEqualTo(participant.getId());
        assertThat(participantConfigResponse.getPayload().getDid()).isEqualTo("did:web:" + this.randomUUID);
        assertThat(participantConfigResponse.getPayload().getEmail()).isEqualTo(EMAIL);
    }
    
    @Test
    void get_participant_profile_200() {
        this.update_participant_profile_image_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        CommonResponse<ParticipantProfileDto> participantConfigResponse = this.participantController.getParticipantProfile(participant.getId().toString());
        assertThat(participantConfigResponse.getPayload().getId()).isEqualTo(participant.getId().toString());
        assertThat(participantConfigResponse.getPayload().getLegalName()).isEqualTo(participant.getLegalName());
        assertThat(participantConfigResponse.getPayload().getEmail()).isEqualTo(participant.getEmail());
        assertThat(participantConfigResponse.getPayload().getShortName()).isEqualTo(participant.getShortName());
        assertThat(participantConfigResponse.getPayload().getProfileImage()).isNotNull();
    }
    
    @Test
    void update_participant_profile_image_400() {
        this.initiate_onboarding_participant_own_did_200();
        
        Participant participant = this.participantRepository.findAll().get(0);
        String participantId = participant.getId().toString();
        FileUploadRequest updateProfileImageRequest = HelperService.getValidUpdateProfileImageRequest();
        
        try (MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class)) {
            fileUtilsMockedStatic.when(() -> FileUtils.copyToFile(any(), any()))
                    .thenThrow(new IOException());
            assertThrows(BadDataException.class, () -> this.participantController.updateParticipantProfileImage(participantId, updateProfileImageRequest));
        }
    }
    
    @Test
    void update_participant_profile_image_200() {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        CommonResponse<Map<String, Object>> participantImageUploadResponse = this.participantController.updateParticipantProfileImage(participant.getId().toString(), HelperService.getValidUpdateProfileImageRequest());
        assertThat(participantImageUploadResponse.getPayload().get("imageUrl")).isNotNull();
    }
    
    @Test
    void update_participant_profile_image_existing_image_200() {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        this.participantController.updateParticipantProfileImage(participant.getId().toString(), HelperService.getValidUpdateProfileImageRequest());

//       Update picture with deleting existing picture
        CommonResponse<Map<String, Object>> participantImageUploadResponse = this.participantController.updateParticipantProfileImage(participant.getId().toString(), HelperService.getValidUpdateProfileImageRequest());
        assertThat(participantImageUploadResponse.getPayload().get("imageUrl")).isNotNull();
    }
    
    @Test
    void delete_participant_profile_image_200() {
        this.update_participant_profile_image_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        assertDoesNotThrow(() -> this.participantController.deleteParticipantProfileImage(participant.getId().toString()));
    }
    
    @Test
    void delete_participant_profile_image_400() {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        assertThrowsExactly(BadDataException.class, () -> this.participantController.deleteParticipantProfileImage(participant.getId().toString()));
    }
    
    @Test
    void export_participant_and_key_own_did_200() {
        this.initiate_onboarding_participant_own_did_200();
        Participant participant = this.participantRepository.findAll().get(0);
        
        JwtAuthenticationToken mockPrincipal = Mockito.mock(JwtAuthenticationToken.class);
        doReturn(Map.of(ID, participant.getId().toString())).when(mockPrincipal).getTokenAttributes();
        
        CommonResponse<ParticipantAndKeyResponse> participantAndKeyResponse = this.participantController.exportParticipantAndKey(participant.getId().toString(), mockPrincipal);
        Credential legalParticipantCredential = this.credentialRepository.findByParticipantIdAndCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        
        assertThat(participantAndKeyResponse.getPayload().getParticipantJson()).isEqualTo(legalParticipantCredential.getVcUrl());
    }
    
    @Test
    void export_participant_and_key_no_did_200() {
        // mimics unit test behaviour
        this.initiate_onboarding_participant_no_did_200();
        
        Participant participant = this.participantRepository.findAll().get(0);
        participant.setOwnDidSolution(false);
        this.participantRepository.save(participant);
        
        doReturn(Credential.builder()
                .participantId(participant.getId())
                .participant(participant)
                .credentialType(CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType())
                .vcUrl("http://localhost/" + this.randomUUID)
                .build()
        ).when(this.credentialRepository).findByParticipantIdAndCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        
        JwtAuthenticationToken mockPrincipal = Mockito.mock(JwtAuthenticationToken.class);
        doReturn(Map.of(ID, participant.getId().toString())).when(mockPrincipal).getTokenAttributes();
        
        CommonResponse<ParticipantAndKeyResponse> participantAndKeyResponse = this.participantController.exportParticipantAndKey(participant.getId().toString(), mockPrincipal);
        Credential legalParticipantCredential = this.credentialRepository.findByParticipantIdAndCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
        
        assertThat(participantAndKeyResponse.getPayload().getParticipantJson()).isEqualTo(legalParticipantCredential.getVcUrl());
    }
    
}
