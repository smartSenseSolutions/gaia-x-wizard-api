package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.core.service.participant.InvokeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.api.utils.StringPool.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PolicyServiceUnitTest {

    private ObjectMapper objectMapper;
    private PolicyService policyService;
    @Mock
    private S3Utils s3Utils;

    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.objectMapper = this.configureObjectMapper();
        ContextConfig contextConfig = new ContextConfig(null, null, null, null, null, List.of("http://www.w3.org/ns/odrl.jsonld", "https://www.w3.org/ns/odrl/2/ODRL22.json"), null);
        this.policyService = Mockito.spy(new PolicyService(this.objectMapper, this.s3Utils, contextConfig));
    }

    @AfterEach
    void tearDown() {
        this.objectMapper = null;
        this.policyService = null;
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
    void testCreateServiceOfferPolicy() {
        ODRLPolicyRequest odrlPolicyRequest = new ODRLPolicyRequest(List.of("BE-BRU"), SPATIAL, this.randomUUID, "did:web:smartx.com", "http://loclahost:8080", this.randomUUID);

        Map<String, Object> serviceOfferPolicy = this.policyService.createServiceOfferPolicy(odrlPolicyRequest, this.randomUUID);
        assertThat(serviceOfferPolicy).isNotNull()
                .hasFieldOrPropertyWithValue(TYPE, "policy")
                .hasFieldOrPropertyWithValue(ID, this.randomUUID);
        try {
            System.out.println(this.objectMapper.writeValueAsString(serviceOfferPolicy));
        } catch (Exception ignored) {

        }
    }

    @Test
    void testGetLocationByServiceOfferingId() {
        doReturn(this.getServiceOfferVc()).when(this.policyService).getServiceOffering(anyString(), anyString());

        try (MockedStatic<InvokeService> invokeServiceMockedStatic = Mockito.mockStatic(InvokeService.class)) {
            invokeServiceMockedStatic.when(() -> InvokeService.executeRequest(anyString(), any())).thenReturn(this.getPolicyJsonString());
            String[] locationByServiceOfferingId = this.policyService.getLocationByServiceOfferingId(this.randomUUID);
            assertThat(locationByServiceOfferingId[0]).isEqualTo("BE-BRU");
        }
    }

    @Test
    void testEvaluatePolicy() {
        doReturn(this.getServiceOfferVc()).when(this.policyService).getServiceOffering(anyString(), anyString());

        try (MockedStatic<InvokeService> invokeServiceMockedStatic = Mockito.mockStatic(InvokeService.class)) {
            invokeServiceMockedStatic.when(() -> InvokeService.executeRequest(anyString(), any())).thenReturn(this.getPolicyJsonString());
            boolean spatialEvaluation = this.policyService.evaluatePolicy(new PolicyEvaluationRequest(this.randomUUID, this.randomUUID));
            assertThat(spatialEvaluation).isTrue();
        }
    }

    private String getPolicyJsonString() {
        return "{\"permission\":[{\"assigner\":\"did:web:smartx.com\",\"action\":\"use\",\"constraint\":[{\"rightOperand\":[\"BE-BRU\"],\"name\":\"spatial\",\"operator\":\"isAnyOf\"}],\"target\":\"1d8a1ea7-9d63-4780-a8a5-5060d3c01bdc\"}],\"id\":\"1d8a1ea7-9d63-4780-a8a5-5060d3c01bdc\",\"type\":\"policy\",\"@context\":[\"http://www.w3.org/ns/odrl.jsonld\",\"https://www.w3.org/ns/odrl/2/ODRL22.json\"]}";
    }

    private JsonNode getServiceOfferVc() {
        final String serviceOfferVcString = "{\"selfDescriptionCredential\":{\"@context\":\"https://www.w3.org/2018/credentials/v1\",\"type\":[\"VerifiablePresentation\"],\"verifiableCredential\":[{\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"credentialSubject\":{\"@Context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"gx:termsAndConditions\":\"The PARTICIPANT signing the Self-Description agrees as follows:\\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\\n\\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).\",\"id\":\"https://sscspl.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/participant.json#2\",\"type\":\"gx:GaiaXTermsAndConditions\"},\"id\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"issuanceDate\":\"2023-09-15T13:37:08.264138715Z\",\"issuer\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"type\":[\"VerifiableCredential\"]},{\"issuanceDate\":\"2023-09-15T13:42:37.550764021Z\",\"credentialSubject\":{\"type\":\"gx:PhysicalResource\",\"gx:name\":\"Nissan\",\"gx:description\":\"The Nissan Micra replaced the Japanese-market Nissan Cherry. It was exclusive to Nissan Japanese dealership network Nissan Cherry Store until 1999 when the \\\"Cherry\\\" network was combined into Nissan Red Stage until 2003. \",\"gx:maintainedBy\":[{\"id\":\"https://nissan.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/participant.json#0\"},{\"id\":\"https://mercedes.dev.smart-x.smartsenselabs.com/c314e97f-fd7a-4162-bb7a-f4ee1c5891bd/participant.json#0\"}],\"gx:locationAddress\":[{\"gx:countryCode\":\"DE-BE\"}],\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/resource_87af6309-ebe6-4038-a4e6-733617f94703.json\"},\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/resource_87af6309-ebe6-4038-a4e6-733617f94703.json\",\"type\":[\"VerifiableCredential\"],\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#\"],\"issuer\":\"did:web:nissan.dev.smart-x.smartsenselabs.com\"},{\"type\":\"VerifiableCredential\",\"id\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"issuer\":\"did:web:sscspl.dev.smart-x.smartsenselabs.com\",\"issuanceDate\":\"2023-09-15T13:44:06.883841578Z\",\"credentialSubject\":{\"gx:termsAndConditions\":{\"gx:URL\":\"https://www.smartsensesolutions.com/privacy-policy\",\"gx:hash\":\"8d70597183df65c4e5f700fd753026f9d75b5b63d32d00bd28774f7537d89af5\"},\"gx:policy\":[\"https://wizard-api.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/service_swzt_policy.json\"],\"gx:dataAccountExport\":{\"gx:requestType\":\"Support Center\",\"gx:accessType\":\"Digital\",\"gx:formatType\":[\"application/3gpdash-qoe-report+xml\"]},\"gx:aggregationOf\":[{\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/0861008f-1d6b-4b0d-b666-3ee8683ade40/resource_87af6309-ebe6-4038-a4e6-733617f94703.json\"}],\"gx:dataProtectionRegime\":[\"LGPD2019\",\"GDPR2016\",\"PDPA2012\",\"CCPA2018\",\"VCDPA2021\"],\"type\":\"gx:ServiceOffering\",\"gx:labelLevel\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/labelLevel_54ad3541-ef23-4463-bb04-4ac4729307df.json\",\"gx:providedBy\":{\"id\":\"https://sscspl.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/participant.json#0\"},\"id\":\"https://wizard-api.dev.smart-x.smartsenselabs.com/42573548-3816-4558-a8b1-8bcf70232912/service_swzt.json\",\"gx:name\":\"Federated Catalogue - SS Dev\",\"gx:description\":\"Federated Catalogue developed by Smart Sense for the Dev environment\"},\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://w3id.org/security/suites/jws-2020/v1\"]}]}}";

        try {
            return this.objectMapper.readTree(serviceOfferVcString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}