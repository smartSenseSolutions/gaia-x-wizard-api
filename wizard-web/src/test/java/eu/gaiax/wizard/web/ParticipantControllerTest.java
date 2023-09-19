package eu.gaiax.wizard.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.core.service.data_master.EntityTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import eu.gaiax.wizard.util.HelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.util.Map;

import static eu.gaiax.wizard.util.constant.TestConstant.LEGAL_NAME;
import static eu.gaiax.wizard.util.constant.TestConstant.SHORT_NAME;
import static eu.gaiax.wizard.utils.WizardRestConstant.REGISTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @BeforeEach
    @BeforeTransaction
    public final void before() {
        this.participantRepository.deleteAll();
    }

    @Test
    void register_participant_200() {
        String entityTypeId = this.entityTypeMasterService.getAll().get(0).getId().toString();
        String subDivision = this.subdivisionCodeMasterService.getAll().get(0).getSubdivisionCode().toString();
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
}
