package eu.gaiax.wizard.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {GaiaXWizardApplication.class})
@ActiveProfiles("test")
@ContextConfiguration(initializers = {ContainerContextInitializer.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceOfferControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ServiceOfferRepository serviceOfferRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    @BeforeTransaction
    public final void before() {
        this.serviceOfferRepository.deleteAll();
    }

    /*@Test
    void add_serviceOffer_200() {
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
    }*/

}
