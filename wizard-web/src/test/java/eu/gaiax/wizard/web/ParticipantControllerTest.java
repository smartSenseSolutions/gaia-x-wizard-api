package eu.gaiax.wizard.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.request.ParticipantCreationRequest;
import eu.gaiax.wizard.api.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.api.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.controller.ParticipantController;
import eu.gaiax.wizard.core.service.data_master.EntityTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.core.service.participant.InvokeService;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import eu.gaiax.wizard.util.HelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.api.utils.StringPool.*;
import static eu.gaiax.wizard.util.constant.TestConstant.*;
import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    @BeforeTransaction
    public final void before() {
        this.participantRepository.deleteAll();
    }

    @Test
    void register_participant_200() {
        doReturn(this.getValidateRegistrationNumberResponse()).when(this.signerClient).validateRegistrationNumber(anyMap());
        doReturn(this.getVerifyUrlResponseMock()).when(this.signerClient).verify(any());

        String entityTypeId = this.entityTypeMasterService.getAll().get(0).getId().toString();
        String subDivision = this.subdivisionCodeMasterService.getAll().get(0).getSubdivisionCode();
        ParticipantRegisterRequest request = HelperService.registerParticipantRequest(LEGAL_NAME, SHORT_NAME, entityTypeId,
                HelperService.prepareDefaultCredential(LEGAL_NAME, subDivision, subDivision),
                false, false, true);
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange(REGISTER, POST, new HttpEntity<>(request), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();
        assertEquals(request.email(), payload.get("email"));
        assertEquals(request.onboardRequest().legalName(), payload.get("legalName"));
        assertEquals(request.onboardRequest().shortName(), payload.get("shortName"));
        assertNotNull(payload.get("credentialRequest"));
    }

    private ResponseEntity<Map<String, Object>> getValidateRegistrationNumberResponse() {
        Map<String, Object> validateRegistrationResponseMap = new HashMap<>();
        validateRegistrationResponseMap.put(DATA, Map.of(IS_VALID, true));
        return ResponseEntity.ok(validateRegistrationResponseMap);
    }

    private ResponseEntity<JsonNode> getVerifyUrlResponseMock() {
        Map<String, Object> signerResponseMap = new HashMap<>();
        String randomUUID = UUID.randomUUID().toString();
        signerResponseMap.put(DATA, Map.of(VERIFY_URL_TYPE, GX_LEGAL_PARTICIPANT));
        signerResponseMap.put("message", randomUUID);
        return ResponseEntity.ok(this.mapper.valueToTree(signerResponseMap));
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
        doReturn(this.getValidateRegistrationNumberResponse()).when(this.signerClient).validateRegistrationNumber(anyMap());
        doReturn(this.getVerifyUrlResponseMock()).when(this.signerClient).verify(any());

        String entityTypeId = this.entityTypeMasterService.getAll().get(0).getId().toString();
        String subDivision = this.subdivisionCodeMasterService.getAll().get(0).getSubdivisionCode();
        ParticipantRegisterRequest request = HelperService.registerParticipantRequest(LEGAL_NAME, SHORT_NAME, entityTypeId,
                HelperService.prepareDefaultCredential(LEGAL_NAME, subDivision, subDivision),
                false, false, true);
        this.restTemplate.exchange(REGISTER, POST, new HttpEntity<>(request), CommonResponse.class);

        ResponseEntity<CommonResponse> response = this.restTemplate.getForEntity(URI.create(CHECK_REGISTRATION + "?email=" + EMAIL), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();

        assertTrue((Boolean) payload.get("userRegistered"));
        assertFalse((Boolean) payload.get("deviceConfigured"));
    }

    @Test
    void initiate_onboarding_participant_200() {
        doReturn(this.getValidateRegistrationNumberResponse()).when(this.signerClient).validateRegistrationNumber(anyMap());
        doReturn(this.getVerifyUrlResponseMock()).when(this.signerClient).verify(any());

        String entityTypeId = this.entityTypeMasterService.getAll().get(0).getId().toString();
        String subDivision = this.subdivisionCodeMasterService.getAll().get(0).getSubdivisionCode();
        ParticipantRegisterRequest request = HelperService.registerParticipantRequest(LEGAL_NAME, SHORT_NAME, entityTypeId,
                HelperService.prepareDefaultCredential(LEGAL_NAME, subDivision, subDivision),
                false, false, true);
        ResponseEntity<CommonResponse> createParticipantResponse = this.restTemplate.exchange(REGISTER, POST, new HttpEntity<>(request), CommonResponse.class);
        Map<String, Object> createParticipantPayload = (Map<String, Object>) createParticipantResponse.getBody().getPayload();
        String participantId = createParticipantPayload.get(ID).toString();

        Map<String, Object> validateDidResponse = new HashMap<>();
        validateDidResponse.put(DATA, Map.of(IS_VALID, true));
        doReturn(ResponseEntity.ok(validateDidResponse)).when(this.signerClient).validateDid(any());


        Map<String, Object> vcMap = new HashMap<>();
        vcMap.put(DATA, Map.of(this.randomUUID, this.randomUUID));
        doReturn(ResponseEntity.ok(vcMap)).when(this.signerClient).createVc(any());

        ParticipantCreationRequest participantCreationRequest = new ParticipantCreationRequest(true, this.randomUUID, this.randomUUID, this.randomUUID, true);
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange(ONBOARD_PARTICIPANT.replace("{participantId}", participantId), POST, new HttpEntity<>(participantCreationRequest), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();

        assertEquals(participantCreationRequest.issuer(), payload.get("did"));
        assertEquals(participantCreationRequest.ownDid(), payload.get("ownDidSolution"));
        assertEquals(participantCreationRequest.store(), payload.get("keyStored"));
    }

    @Test
    void validate_participant_200() {
        doReturn(this.getVerifyUrlResponseMock()).when(this.signerClient).verify(any());

        ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest("http://localhost/" + this.randomUUID, "did:web:" + this.randomUUID, this.randomUUID, false, false);

        try (MockedStatic<InvokeService> invokeServiceMockedStatic = Mockito.mockStatic(InvokeService.class)) {
            invokeServiceMockedStatic.when(() -> InvokeService.executeRequest(anyString(), any())).thenReturn(this.generateLegalParticipantMock());
            String response = this.participantController.validateParticipant(participantValidatorRequest);
            assertEquals("Success", response);
        }

    }

    @Test
    void get_wellKnown_files_200() {
        this.initiate_onboarding_participant_200();
        this.participantRepository.findAll().get(0);
    }

    String generateLegalParticipantMock() {
        return "{\"selfDescriptionCredential\":{\"verifiableCredential\":[{\"issuer\":\"did:web:" + this.randomUUID + "\"}]}}";
    }
}
