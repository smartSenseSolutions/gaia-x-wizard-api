package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.api.client.MessagingQueueClient;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {GaiaXWizardApplication.class})
@ActiveProfiles("test")
@ContextConfiguration(initializers = {ContainerContextInitializer.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublishServiceTest {

    private PublishService publishService;

    @MockBean
    @Autowired
    private ServiceOfferRepository serviceOfferRepository;

    @MockBean
    @Autowired
    private MessagingQueueClient messagingQueueClient;

    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.publishService = new PublishService(this.configureObjectMapper(), this.messagingQueueClient, this.serviceOfferRepository);
    }

    private ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Test
    void testPublishServiceComplianceToMessagingQueue() {
        doReturn(this.generateSuccessResponseEntity()).when(this.messagingQueueClient).publishServiceCompliance(any());
        doNothing().when(this.serviceOfferRepository).updateMessageReferenceId(any(), anyString());

        assertDoesNotThrow(() -> this.publishService.publishServiceComplianceToMessagingQueue(UUID.randomUUID(), this.getServiceCompliance()));
    }

    private ResponseEntity<Object> generateSuccessResponseEntity() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("location", "http://localhost/" + this.randomUUID);
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
    }

    private String getServiceCompliance() {
        return "{\"complianceCredential\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"type\":[\"VerifiableCredential\"],\"id\":\"https://compliance.lab.gaia-x.eu/development/credential-offers/55427329-5d44-43cb-a425-308f874a4dc9\",\"issuer\":\"did:web:compliance.lab.gaia-x.eu:development\",\"issuanceDate\":\"2023-08-21T08:40:14.393Z\",\"expirationDate\":\"2023-11-19T08:40:14.393Z\",\"credentialSubject\":[{\"type\":\"gx:compliance\",\"id\":\"https://example.com/125/participant.json#0\",\"integrity\":\"sha256-372994ed4b18b7f4252626a48c510aa170cb3a04717d2501ccf1982ed85a9b12\",\"version\":\"22.10\"},{\"type\":\"gx:compliance\",\"id\":\"https://example.com/125/participant.json#1\",\"integrity\":\"sha256-20059a7a182d8a840ee25f8773446aa2f0564c0ff82d359b8a1b194bf1f98045\",\"version\":\"22.10\"},{\"type\":\"gx:compliance\",\"id\":\"https://example.com/125/participant.json#2\",\"integrity\":\"sha256-f897639cb8a0236874ec395a3b0443986fcf5e3b1ac4ac1f3978e72e685dea2f\",\"version\":\"22.10\"},{\"type\":\"gx:compliance\",\"id\":\"https://example.com/125/service_Fmai.json\",\"integrity\":\"sha256-7fd1168cf7fd7c3e36c8293db3e04938c750b06992b779cbc7b828fc7eb060c3\",\"version\":\"22.10\"},{\"type\":\"gx:compliance\",\"id\":\"https://example.com/125/service_S7GZ.json\",\"integrity\":\"sha256-70515740d132923073c25fba85b1af920164ac7ec94d9cd08cb99b5374d49edf\",\"version\":\"22.10\"}],\"proof\":{\"type\":\"JsonWebSignature2020\",\"created\":\"2023-08-21T08:40:15.041Z\",\"proofPurpose\":\"assertionMethod\",\"jws\":\"\",\"verificationMethod\":\"did:web:compliance.lab.gaia-x.eu:development#X509-JWK2020\"}}}";
    }
}