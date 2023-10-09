package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ServiceFilterResponse;
import eu.gaiax.wizard.api.model.policy.SubdivisionName;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceDetailResponse;
import eu.gaiax.wizard.api.model.service_offer.ServiceIdRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceOfferResponse;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.InvokeService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static eu.gaiax.wizard.api.utils.StringPool.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
class ServiceOfferServiceUnitTest {

    @Mock
    private ServiceOfferRepository serviceOfferRepository;
    @Mock
    private ParticipantService participantService;
    @Mock
    private PolicyService policyService;
    @Mock
    private SignerService signerService;
    @Mock
    private ServiceLabelLevelService serviceLabelLevelService;
    @Mock
    private CredentialService credentialService;
    @Mock
    private PublishService publishService;
    @Mock
    private SubdivisionCodeMasterService subdivisionCodeMasterService;
    private ServiceOfferService serviceOfferService;
    private final String randomUUID = UUID.randomUUID().toString();

    private CreateServiceOfferingRequest createServiceOfferingRequest;
    private Credential credential;
    private ServiceOffer serviceOffer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = this.configureObjectMapper();
        this.serviceOfferService = Mockito.spy(new ServiceOfferService(this.credentialService, this.serviceOfferRepository, this.objectMapper,
                this.participantService, this.signerService, this.policyService, null, null, null, this.serviceLabelLevelService,
                null, null, this.publishService, this.subdivisionCodeMasterService));
        this.createServiceOfferingRequest = this.generateMockServiceOfferRequest();
        this.credential = this.generateMockCredential();
        this.serviceOffer = this.generateMockServiceOffer();
    }

    private ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @AfterEach
    void tearDown() {
        this.createServiceOfferingRequest = null;
        this.credential = null;
        this.serviceOffer = null;
    }

    @Test
    void testCreateServiceOffering() throws IOException {
        Participant participant = this.generateMockParticipant();

        Mockito.doNothing().when(this.serviceOfferService).validateServiceOfferMainRequest(any());
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());
        doReturn(participant).when(this.participantService).findParticipantById(any());
        Mockito.doNothing().when(this.signerService).validateRequestUrl(Collections.singletonList(this.randomUUID), List.of(GX_LEGAL_PARTICIPANT), null, "participant.url.not.found", null);
        Mockito.doNothing().when(this.policyService).hostPolicy(anyString(), anyString());
        Mockito.doNothing().when(this.publishService).publishServiceComplianceToMessagingQueue(any(), anyString());
        Mockito.doReturn(this.credential).when(this.credentialService).createCredential(anyString(), anyString(), anyString(), anyString(), any());
        Mockito.doReturn(this.getServiceCredentialMock()).when(this.signerService).signService(any(), any(), anyString());
        Mockito.doReturn(this.serviceOffer).when(this.serviceOfferRepository).save(any());

        try (MockedStatic<HashingService> hashingServiceMockedStatic = Mockito.mockStatic(HashingService.class)) {
            hashingServiceMockedStatic.when(() -> HashingService.fetchJsonContent(anyString())).thenReturn(this.randomUUID);
            ServiceOfferResponse responseServiceOffer = this.serviceOfferService.createServiceOffering(this.createServiceOfferingRequest, UUID.randomUUID().toString(), false);
            assertThat(responseServiceOffer.getName()).isEqualTo(this.createServiceOfferingRequest.getName());
        }

    }

    @Test
    void testGetLocationFromService() {
        doReturn(new String[]{"BE-BRU"}).when(this.policyService).getLocationByServiceOfferingId(anyString());
        SubdivisionName subdivisionName = new SubdivisionName("Brussels");
        doReturn(Collections.singletonList(subdivisionName)).when(this.subdivisionCodeMasterService).getNameListBySubdivisionCode(any());

        List<String> locationListFromService = this.serviceOfferService.getLocationFromService(new ServiceIdRequest(this.randomUUID));
        assertThat(locationListFromService.get(0)).isEqualTo(subdivisionName.name());
    }

   /* @Test
    void testValidateServiceOfferMainRequest() {
    }*/

    @Test
    void testFilterServiceOffering() {
        doReturn(new PageImpl<>(Collections.singletonList(this.serviceOffer))).when(this.serviceOfferService).filter(any());
        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(1);
        PageResponse<ServiceFilterResponse> serviceOfferFilterResponse = this.serviceOfferService.filterServiceOffering(filterRequest, null);

        assertThat(serviceOfferFilterResponse.getContent().iterator().next().getName()).isEqualTo(this.serviceOffer.getName());
    }

    @Test
    void testGetServiceOfferingById() {
        doReturn(Optional.of(this.serviceOffer)).when(this.serviceOfferRepository).findById(any());
        doReturn(new String[]{"BE-BRU"}).when(this.policyService).getLocationByServiceOfferingId(anyString());

        try (MockedStatic<InvokeService> invokeServiceMockedStatic = Mockito.mockStatic(InvokeService.class)) {
            invokeServiceMockedStatic.when(() -> InvokeService.executeRequest(anyString(), any())).thenReturn(this.getServiceOfferVc());
            ServiceDetailResponse serviceOfferingById = this.serviceOfferService.getServiceOfferingById(UUID.fromString(this.randomUUID));
            assertThat(serviceOfferingById.getName()).isEqualTo(this.serviceOffer.getName());
        }
    }

    private CreateServiceOfferingRequest generateMockServiceOfferRequest() {
        CreateServiceOfferingRequest createServiceOfferingRequest = new CreateServiceOfferingRequest();
        createServiceOfferingRequest.setName(this.randomUUID);

        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put(GX_POLICY, Map.of("gx:location", Collections.singletonList(this.randomUUID)));
        credentialSubject.put(AGGREGATION_OF, this.randomUUID);
        credentialSubject.put(GX_TERMS_AND_CONDITIONS, Map.of("gx:URL", this.randomUUID));

        Map<String, Object> dataExport = new HashMap<>();
        dataExport.put(GX_REQUEST_TYPE, this.randomUUID);
        dataExport.put(GX_ACCESS_TYPE, this.randomUUID);
        dataExport.put(GX_FORMAT_TYPE, this.randomUUID);

        credentialSubject.put(GX_DATA_ACCOUNT_EXPORT, dataExport);
        createServiceOfferingRequest.setCredentialSubject(credentialSubject);

        return createServiceOfferingRequest;
    }

    private Credential generateMockCredential() {
        return Credential.builder()
                .vcUrl(this.randomUUID)
                .vcJson("{}")
                .participantId(UUID.fromString(this.randomUUID))
                .build();
    }

    private Participant generateMockParticipant() {
        Participant participant = new Participant();
        participant.setId(UUID.fromString(this.randomUUID));
        participant.setOwnDidSolution(true);
        return participant;
    }

    private ServiceOffer generateMockServiceOffer() {
        ServiceOffer serviceOffer = new ServiceOffer();
        serviceOffer.setId(UUID.fromString(this.randomUUID));
        serviceOffer.setName(this.randomUUID);
        serviceOffer.setCredential(this.credential);
        serviceOffer.setVeracityData("{\"" + TRUST_INDEX + "\":\" 0.258 \"}");
        return serviceOffer;
    }

    private Map<String, String> getServiceCredentialMock() {
        Map<String, Object> selfDescriptionCredential = new HashMap<>();
        Map<String, Object> serviceOfferVerifiableCredential = new HashMap<>();
        serviceOfferVerifiableCredential.put(CREDENTIAL_SUBJECT, Map.of(TYPE, GX_SERVICE_OFFERING));
        selfDescriptionCredential.put(VERIFIABLE_CREDENTIAL_CAMEL_CASE, Collections.singletonList(serviceOfferVerifiableCredential));

        Map<String, String> serviceCredential = new HashMap<>();
        try {
            serviceCredential.put(SERVICE_VC, this.objectMapper.writeValueAsString(Map.of(SELF_DESCRIPTION_CREDENTIAL, selfDescriptionCredential)));
        } catch (Exception ignored) {

        }
        return serviceCredential;
    }

    private String getServiceOfferVc() {
        return "{\"selfDescriptionCredential\":{\"@context\":\"https://www.w3.org/2018/credentials/v1\",\"type\":[\"VerifiablePresentation\"],\"verifiableCredential\":[{\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"credentialSubject\":{\"@Context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"gx:termsAndConditions\":\"The PARTICIPANT signing the Self-Description agrees as follows:\\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\\n\\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).\",\"id\":\"https://sscspl.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/participant.json#2\",\"type\":\"gx:GaiaXTermsAndConditions\"},\"id\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"issuanceDate\":\"2023-09-15T13:37:08.264138715Z\",\"issuer\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"type\":[\"VerifiableCredential\"]},{\"issuanceDate\":\"2023-09-15T13:42:37.550764021Z\",\"credentialSubject\":{\"type\":\"gx:PhysicalResource\",\"gx:name\":\"Nissan\",\"gx:description\":\"The Nissan Micra replaced the Japanese-market Nissan Cherry. It was exclusive to Nissan Japanese dealership network Nissan Cherry Store until 1999 when the \\\"Cherry\\\" network was combined into Nissan Red Stage until 2003. \",\"gx:maintainedBy\":[{\"id\":\"https://nissan.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/participant.json#0\"},{\"id\":\"https://mercedes.dev.smart-x.smartsenselabs.com/c314e97f-fd7a-4162-bb7a-f4ee1c5891bd/participant.json#0\"}],\"gx:locationAddress\":[{\"gx:countryCode\":\"DE-BE\"}],\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/resource_87af6309-ebe6-4038-a4e6-733617f94703.json\"},\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/resource_87af6309-ebe6-4038-a4e6-733617f94703.json\",\"type\":[\"VerifiableCredential\"],\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"issuer\":\"did:web:nissan.dev.smart-x.smartsenselabs.com\"},{\"type\":\"VerifiableCredential\",\"id\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"issuer\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"issuanceDate\":\"2023-09-15T13:44:06.883841578Z\",\"credentialSubject\":{\"gx:termsAndConditions\":{\"gx:URL\":\"https://www.smartsensesolutions.com/privacy-policy\",\"gx:hash\":\"8d70597183df65c4e5f700fd753026f9d75b5b63d32d00bd28774f7537d89af5\"},\"gx:policy\":[\"https://wizard-api.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/service_swzt_policy.json\"],\"gx:dataAccountExport\":{\"gx:requestType\":\"Support Center\",\"gx:accessType\":\"Digital\",\"gx:formatType\":[\"application/3gpdash-qoe-report+xml\"]},\"gx:aggregationOf\":[{\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/resource_87af6309-ebe6-4038-a4e6-733617f94703.json\"}],\"gx:dataProtectionRegime\":[\"LGPD2019\",\"GDPR2016\",\"PDPA2012\",\"CCPA2018\",\"VCDPA2021\"],\"type\":\"gx:ServiceOffering\",\"gx:labelLevel\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/labelLevel_54ad3541-ef23-4463-bb04-4ac4729307df.json\",\"gx:providedBy\":{\"id\":\"https://sscspl.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/participant.json#0\"},\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/service_swzt.json\",\"gx:name\":\"Federated Catalogue - SS Dev\",\"gx:description\":\"Federated Catalogue developed by Smart Sense for the Dev environment\"},\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\"]}]}}";
    }

}
