package eu.gaiax.wizard.core.service.signer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.RemoteServiceException;
import eu.gaiax.wizard.api.model.did.ServiceEndpointConfig;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.core.service.participant.InvokeService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static eu.gaiax.wizard.api.utils.StringPool.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({OutputCaptureExtension.class, MockitoExtension.class})
class SignerServiceTest {

    private ContextConfig contextConfig;
    @Mock
    private CredentialService credentialService;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private SignerClient signerClient;
    @Mock
    private S3Utils s3Utils;
    private ObjectMapper objectMapper;
    @Mock
    private ScheduleService scheduleService;

    @Mock
    private MessageSource messageSource;

    private SignerService signerService;

    private final String randomUUID = UUID.randomUUID().toString();

    private Participant participant;

    @BeforeEach
    void setUp() {
        System.setProperty("wizard.gaiax.tnc", "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document or a typeface without relying on meaningful content.");
        this.participant = this.generateMockParticipant();
        this.objectMapper = this.configureObjectMapper();
        this.contextConfig = new ContextConfig(
                List.of("https://www.w3.org/2018/credentials/v1", "https://w3id.org/security/suites/jws-2020/v1"),
                List.of("https://www.w3.org/2018/credentials/v1", "https://w3id.org/security/suites/jws-2020/v1", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"),
                List.of("https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/participant"),
                List.of("https://www.w3.org/2018/credentials/v1,https://w3id.org/security/suites/jws-2020/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"),
                List.of("https://www.w3.org/2018/credentials/v1,https://w3id.org/security/suites/jws-2020/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"),
                List.of("http://www.w3.org/ns/odrl.jsonld,https://www.w3.org/ns/odrl/2/ODRL22.json"),
                List.of("https://www.w3.org/2018/credentials/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#")
        );
        ServiceEndpointConfig serviceEndpointConfig = new ServiceEndpointConfig(this.randomUUID, this.randomUUID, this.randomUUID);
        this.signerService = Mockito.spy(new SignerService(this.contextConfig, this.credentialService, this.participantRepository, this.signerClient,
                this.s3Utils, this.objectMapper, this.scheduleService, serviceEndpointConfig, this.messageSource, List.of("integrityCheck", "holderSignature", "complianceSignature", "complianceCheck"), "http://localhost/", this.randomUUID));
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
        this.objectMapper = null;
        this.contextConfig = null;
        this.signerService = null;
        this.participant = null;
        System.clearProperty("wizard.gaiax.tnc");
    }

    @Test
    void testCreateParticipantJson_credentialExists(CapturedOutput output) {
        doReturn(Optional.of(this.participant)).when(this.participantRepository).findById(any());
        doReturn(Credential.builder().vcJson(this.randomUUID).vcUrl(this.randomUUID).build()).when(this.credentialService).getLegalParticipantCredential(any());
        this.signerService.createParticipantJson(UUID.fromString(this.randomUUID));

        assertThat(output.getOut()).contains("Legal Participant exists");
    }

    @Test
    void testCreateParticipantJson_credentialDoesNotExist() {
        Map<String, Object> vcMap = new HashMap<>();
        vcMap.put(DATA, Map.of(this.randomUUID, this.randomUUID));
        doReturn(ResponseEntity.ok(vcMap)).when(this.signerClient).createVc(any());

        doReturn(null).when(this.credentialService).createCredential(anyString(), anyString(), anyString(), nullable(String.class), any());
        doReturn(this.participant).when(this.participantRepository).save(any());
        doNothing().when(this.s3Utils).uploadFile(anyString(), any());

        assertDoesNotThrow(() -> this.signerService.createParticipantJson(this.participant, this.randomUUID, this.randomUUID, this.randomUUID, true));
    }

    @Test
    void testCreateDid_withCertificate(CapturedOutput output) throws SchedulerException {
        doReturn(Optional.of(this.participant)).when(this.participantRepository).findById(any());
        doNothing().when(this.s3Utils).uploadFile(anyString(), any());
        doNothing().when(this.scheduleService).createJob(anyString(), anyString(), anyInt());

        Map<String, Object> vcMap = new HashMap<>();
        vcMap.put(DATA, Map.of("did", this.randomUUID));
        doReturn(ResponseEntity.ok(vcMap)).when(this.signerClient).createDid(any());

        try (MockedStatic<InvokeService> invokeServiceMockedStatic = Mockito.mockStatic(InvokeService.class)) {
            invokeServiceMockedStatic.when(() -> InvokeService.executeRequest(anyString(), any())).thenReturn(this.randomUUID);
            assertDoesNotThrow(() -> this.signerService.createDid(UUID.fromString(this.randomUUID)));
            assertThat(output).contains("DID Document has been created");
        }
    }

    @Test
    void testCreateDid_withoutCertificate(CapturedOutput output) throws SchedulerException {
        doReturn(Optional.of(this.participant)).when(this.participantRepository).findById(any());
        doNothing().when(this.scheduleService).createJob(anyString(), anyString(), anyInt());
        doReturn(false).when(this.signerService).fetchX509Certificate(anyString());

        assertDoesNotThrow(() -> this.signerService.createDid(UUID.fromString(this.randomUUID)));
        assertThat(output).contains("DID creation cron has been scheduled.");
    }

    @Test
    void testSignResource() {
        doNothing().when(this.s3Utils).uploadFile(anyString(), any());

        Map<String, Object> signerResponse = new HashMap<>();
        signerResponse.put(DATA, Map.of(COMPLETE_SD, Map.of(this.randomUUID, this.randomUUID)));
        doReturn(ResponseEntity.ok(signerResponse)).when(this.signerClient).signResource(any());

        assertThat(this.signerService.signResource(Map.of(this.randomUUID, this.randomUUID), UUID.fromString(this.randomUUID), this.randomUUID)).isEqualTo("{\"" + this.randomUUID + "\":\"" + this.randomUUID + "\"}");
    }

    @Test
    void testSignLabelLevel() {
        doNothing().when(this.s3Utils).uploadFile(anyString(), any());

        Map<String, Object> signerResponse = new HashMap<>();
        signerResponse.put(DATA, Map.of("selfDescriptionCredential", Map.of(this.randomUUID, this.randomUUID)));
        doReturn(ResponseEntity.ok(signerResponse)).when(this.signerClient).signLabelLevel(any());

        assertThat(this.signerService.signLabelLevel(Map.of(this.randomUUID, this.randomUUID), UUID.fromString(this.randomUUID), this.randomUUID)).isEqualTo("{\"" + this.randomUUID + "\":\"" + this.randomUUID + "\"}");
    }

    @Test
    void testValidateRequestUrl() {
        Map<String, Object> signerResponseMap = new HashMap<>();
        String randomUUID = UUID.randomUUID().toString();
        signerResponseMap.put(DATA, Map.of(VERIFY_URL_TYPE, GX_LEGAL_PARTICIPANT));
        signerResponseMap.put("message", randomUUID);
        doReturn(ResponseEntity.ok(this.objectMapper.valueToTree(signerResponseMap))).when(this.signerClient).verify(any());

        assertDoesNotThrow(() -> this.signerService.validateRequestUrl(List.of(this.randomUUID), List.of(GX_LEGAL_PARTICIPANT), null, "participant.not.found", null));
    }

    @Test
    void testValidateRequestUrl_400() {
        Map<String, Object> signerResponseMap = new HashMap<>();
        String randomUUID = UUID.randomUUID().toString();
        signerResponseMap.put(DATA, Map.of(VERIFY_URL_TYPE, GX_SERVICE_OFFERING));
        signerResponseMap.put("message", randomUUID);
        doReturn(ResponseEntity.ok(this.objectMapper.valueToTree(signerResponseMap))).when(this.signerClient).verify(any());

        List<String> urlList = Collections.singletonList(this.randomUUID);
        List<String> urlTypeList = Collections.singletonList(GX_LEGAL_PARTICIPANT);
        assertThrows(BadDataException.class, () -> this.signerService.validateRequestUrl(urlList, urlTypeList, null, "participant.not.found", null));
    }

    @Test
    void testValidateRequestUrl_remoteException() {
        doThrow(new RemoteServiceException()).when(this.signerClient).verify(any());

        List<String> urlList = Collections.singletonList(this.randomUUID);
        List<String> urlTypeList = Collections.singletonList(GX_LEGAL_PARTICIPANT);
        assertThrows(BadDataException.class, () -> this.signerService.validateRequestUrl(urlList, urlTypeList, null, "participant.not.found", null));
    }

    @Test
    void testSignService() {
        doNothing().when(this.s3Utils).uploadFile(anyString(), any());

        Map<String, Object> serviceOfferVc = new HashMap<>();
        serviceOfferVc.put(COMPLETE_SD, new HashMap<>());
        serviceOfferVc.put(TRUST_INDEX, new HashMap<>());
        doReturn(ResponseEntity.ok(Map.of(DATA, serviceOfferVc))).when(this.signerClient).createServiceOfferVc(any());

        Participant participantWithDid = this.generateMockParticipant();
        participantWithDid.setDid("did:web:" + this.randomUUID);
        Map<String, String> signedService = this.signerService.signService(participantWithDid, this.generateMockServiceOfferRequest(), this.randomUUID);

        assertThat(signedService)
                .containsKey(SERVICE_VC)
                .containsKey(TRUST_INDEX);
    }

    @Test
    void testAddServiceEndpoint() throws IOException {
        doNothing().when(this.s3Utils).uploadFile(anyString(), any());
        doReturn(this.generateMockDidFile()).when(this.s3Utils).getObject(anyString(), anyString());

        assertDoesNotThrow(() -> this.signerService.addServiceEndpoint(UUID.fromString(this.randomUUID), this.randomUUID, this.randomUUID, this.randomUUID));
    }

    private File generateMockDidFile() throws IOException {
        File updatedFile = new File(TEMP_FOLDER + UUID.randomUUID() + JSON_EXTENSION);
        Map<String, Object> didMap = new HashMap<>();
        FileUtils.writeStringToFile(updatedFile, this.objectMapper.writeValueAsString(didMap), Charset.defaultCharset());
        return updatedFile;
    }

    @Test
    void testValidateRegistrationNumber() {
        Map<String, Object> validateDidResponse = new HashMap<>();
        validateDidResponse.put(DATA, Map.of(IS_VALID, true));
        doReturn(ResponseEntity.ok(validateDidResponse)).when(this.signerClient).validateRegistrationNumber(any());

        Map<String, Object> request = new HashMap<>();
        request.put(LEGAL_REGISTRATION_NUMBER, Map.of("gx:vatID", "FR79537407926"));

        boolean isRegistrationNumberValid = this.signerService.validateRegistrationNumber(request);
        assertThat(isRegistrationNumberValid).isTrue();
    }

    @Test
    void testValidateRegistrationNumber_exception() {
        doThrow(new RemoteServiceException()).when(this.signerClient).validateRegistrationNumber(any());

        Map<String, Object> request = new HashMap<>();
        request.put(LEGAL_REGISTRATION_NUMBER, Map.of("gx:vatID", "FR79537407926"));

        boolean isRegistrationNumberValid = this.signerService.validateRegistrationNumber(request);
        assertThat(isRegistrationNumberValid).isFalse();
    }

    @Test
    void testValidateDid() {
        Map<String, Object> validateDidResponse = new HashMap<>();
        validateDidResponse.put(DATA, Map.of(IS_VALID, true));
        doReturn(ResponseEntity.ok(validateDidResponse)).when(this.signerClient).validateDid(any());

        boolean isDidValid = this.signerService.validateDid(this.randomUUID, this.randomUUID, this.randomUUID);
        assertThat(isDidValid).isTrue();
    }

    @Test
    void testValidateDid_exception() {
        doThrow(new RemoteServiceException()).when(this.signerClient).validateDid(any());

        boolean isDidValid = this.signerService.validateDid(this.randomUUID, this.randomUUID, this.randomUUID);
        assertThat(isDidValid).isFalse();
    }

    private Participant generateMockParticipant() {
        Participant participant = new Participant();
        participant.setId(UUID.fromString(this.randomUUID));
        participant.setOwnDidSolution(false);
        participant.setDomain(this.randomUUID);
        participant.setCredentialRequest("{\"legalParticipant\":{\"credentialSubject\":{\"gx:legalName\":\"Participant Example\",\"gx:headquarterAddress\":{\"gx:countrySubdivisionCode\":\"BE-BRU\"},\"gx:legalAddress\":{\"gx:countrySubdivisionCode\":\"BE-BRU\"}}},\"legalRegistrationNumber\":{\"gx:leiCode\":\"9695007586XZAKPYJ703\"}}");
        return participant;
    }

    private CreateServiceOfferingRequest generateMockServiceOfferRequest() {
        CreateServiceOfferingRequest createServiceOfferingRequest = new CreateServiceOfferingRequest();
        createServiceOfferingRequest.setName(this.randomUUID);

        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put(GX_POLICY, Map.of("gx:location", Collections.singletonList(this.randomUUID)));
        credentialSubject.put(AGGREGATION_OF, Collections.singletonList(Map.of(ID, this.randomUUID)));
        credentialSubject.put(DEPENDS_ON, Collections.singletonList(Map.of(ID, this.randomUUID)));
        credentialSubject.put(GX_TERMS_AND_CONDITIONS, Map.of("gx:URL", this.randomUUID));

        Map<String, Object> dataExport = new HashMap<>();
        dataExport.put(GX_REQUEST_TYPE, this.randomUUID);
        dataExport.put(GX_ACCESS_TYPE, this.randomUUID);
        dataExport.put(GX_FORMAT_TYPE, this.randomUUID);

        credentialSubject.put(GX_DATA_ACCOUNT_EXPORT, dataExport);
        credentialSubject.put(GX_CRITERIA, Map.of(this.randomUUID, this.randomUUID));
        createServiceOfferingRequest.setCredentialSubject(credentialSubject);
        createServiceOfferingRequest.setPrivateKey(this.randomUUID);
        createServiceOfferingRequest.setVerificationMethod("did:web:" + this.randomUUID);

        return createServiceOfferingRequest;
    }
}