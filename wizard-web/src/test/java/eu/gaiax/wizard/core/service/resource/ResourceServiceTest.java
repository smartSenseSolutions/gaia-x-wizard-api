package eu.gaiax.wizard.core.service.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ResourceFilterResponse;
import eu.gaiax.wizard.api.model.ResourceType;
import eu.gaiax.wizard.api.model.did.ServiceEndpointConfig;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.VaultService;
import eu.gaiax.wizard.core.service.service_offer.PolicyService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.resource.ResourceRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static eu.gaiax.wizard.api.utils.StringPool.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private ParticipantService participantService;
    @Mock
    private ParticipantRepository participantRepository;

    private ObjectMapper objectMapper;
    @Mock
    private CredentialService credentialService;
    @Mock
    private SignerService signerService;
    @Mock
    private PolicyService policyService;
    @Mock
    private VaultService vaultService;
    private ResourceService resourceService;
    private Resource resource;
    private Credential credential;

    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.objectMapper = this.configureObjectMapper();
        ContextConfig contextConfig = new ContextConfig(null, null, null, null, null, List.of("http://www.w3.org/ns/odrl.jsonld", "https://www.w3.org/ns/odrl/2/ODRL22.json"), List.of("https://www.w3.org/2018/credentials/v1", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"));
        ServiceEndpointConfig serviceEndpointConfig = new ServiceEndpointConfig(this.randomUUID, this.randomUUID, this.randomUUID);
        this.resourceService = Mockito.spy(new ResourceService(this.resourceRepository, this.participantService, this.vaultService, this.participantRepository, contextConfig,
                this.objectMapper, this.credentialService, null, this.signerService, serviceEndpointConfig, this.policyService));
        this.credential = this.generateMockCredential();
        this.resource = this.generateMockResource();
    }

    @AfterEach
    void tearDown() {
        this.objectMapper = null;
        this.resourceService = null;
        this.credential = null;
        this.resource = null;
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
    void testCreateResource_physical() {
        Participant participant = this.generateMockParticipant();
        participant.setOwnDidSolution(false);

        doReturn(this.credential).when(this.credentialService).createCredential(anyString(), anyString(), anyString(), anyString(), any());
        doReturn(participant).when(this.participantService).validateParticipant(any());
        doReturn(this.resource).when(this.resourceRepository).save(any());
        doReturn(this.getResourceCredentialMock()).when(this.signerService).signResource(anyMap(), any(), anyString());

        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doNothing().when(this.signerService).addServiceEndpoint(any(), anyString(), anyString(), anyString());

        Resource resourceActual = this.resourceService.createResource(this.generateMockCreatePhysicalResourceRequest(), null);
        assertThat(resourceActual.getName()).isEqualTo(this.resource.getName());
    }

    @Test
    void testFilterResource() {

        doReturn(new PageImpl<>(Collections.singletonList(this.resource))).when(this.resourceService).filter(any());
        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(1);
        PageResponse<ResourceFilterResponse> resourceFilterPageResponse = this.resourceService.filterResource(filterRequest, this.randomUUID);

        assertThat(resourceFilterPageResponse.getContent().iterator().next().getName()).isEqualTo(this.resource.getName());
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

    private CreateResourceRequest generateMockCreatePhysicalResourceRequest() {
        CreateResourceRequest createResourceRequest = new CreateResourceRequest();
        createResourceRequest.setStoreVault(false);
        createResourceRequest.setPrivateKey(this.randomUUID);

        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put("gx:description", this.randomUUID);
        credentialSubject.put(TYPE, ResourceType.PHYSICAL_RESOURCE.getValue());
        credentialSubject.put(NAME, this.randomUUID);
        credentialSubject.put(MAINTAINED_BY, Collections.singletonList(Map.of(ID, this.randomUUID)));
        credentialSubject.put(OWNED_BY, Collections.singletonList(Map.of(ID, this.randomUUID)));
        credentialSubject.put(MANUFACTURED_BY, Collections.singletonList(Map.of(ID, this.randomUUID)));

        createResourceRequest.setCredentialSubject(credentialSubject);
        return createResourceRequest;
    }

    private CreateResourceRequest generateMockCreateVirtualDataResourceRequest() {
        CreateResourceRequest createResourceRequest = new CreateResourceRequest();
        createResourceRequest.setStoreVault(false);
        createResourceRequest.setPrivateKey(this.randomUUID);

        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put(TYPE, "VirtualResource");
        credentialSubject.put(SUBTYPE, "VirtualDataResource");
        credentialSubject.put(NAME, this.randomUUID);
        credentialSubject.put("gx:description", this.randomUUID);
        credentialSubject.put("gx:license", "http://localhost");
        credentialSubject.put("gx:containsPII", true);
        credentialSubject.put(LEGAL_BASIS, this.randomUUID);
        credentialSubject.put(GX_EMAIL, this.randomUUID);
        credentialSubject.put(PRODUCED_BY, Map.of(ID, this.randomUUID));

        final String testDate = "2030-12-01 00:00:00.000";
        credentialSubject.put(OBSOLETE_TIME, testDate);
        credentialSubject.put(EXPIRATION_TIME, testDate);

        createResourceRequest.setCredentialSubject(credentialSubject);
        return createResourceRequest;
    }

    private CreateResourceRequest generateMockCreateVirtualSoftwareResourceRequest() {
        CreateResourceRequest createResourceRequest = new CreateResourceRequest();
        createResourceRequest.setStoreVault(false);
        createResourceRequest.setPrivateKey(this.randomUUID);

        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put(TYPE, "VirtualResource");
        credentialSubject.put(SUBTYPE, "VirtualSoftwareResource");
        credentialSubject.put(NAME, this.randomUUID);
        credentialSubject.put("gx:description", this.randomUUID);
        credentialSubject.put("gx:license", "http://localhost");
        credentialSubject.put(COPYRIGHT_OWNED_BY, Collections.singletonList(Map.of(ID, this.randomUUID)));
        credentialSubject.put(AGGREGATION_OF, Collections.singletonList(Map.of(ID, this.randomUUID)));
        credentialSubject.put(GX_POLICY, Map.of(CUSTOM_ATTRIBUTE, this.randomUUID));

        createResourceRequest.setCredentialSubject(credentialSubject);
        return createResourceRequest;
    }

    private Resource generateMockResource() {
        Resource resource = new Resource();
        resource.setName(this.randomUUID);
        resource.setCredential(this.credential);
        resource.setType(ResourceType.PHYSICAL_RESOURCE.getValue());
        return resource;
    }

    private String getResourceCredentialMock() {
        Map<String, Object> selfDescriptionCredential = new HashMap<>();
        Map<String, Object> resourceVerifiableCredential = new HashMap<>();
        resourceVerifiableCredential.put(CREDENTIAL_SUBJECT, Map.of(TYPE, ResourceType.PHYSICAL_RESOURCE.getValue()));
        selfDescriptionCredential.put(VERIFIABLE_CREDENTIAL_CAMEL_CASE, Collections.singletonList(resourceVerifiableCredential));

        try {
            return this.objectMapper.writeValueAsString(Map.of(SELF_DESCRIPTION_CREDENTIAL, selfDescriptionCredential));
        } catch (Exception ignored) {

        }
        return null;
    }

    @Test
    void testCreateResource_virtualData_200() {
        doReturn(this.credential).when(this.credentialService).createCredential(anyString(), anyString(), anyString(), anyString(), any());
        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(Collections.singletonList(this.randomUUID), List.of(GX_LEGAL_PARTICIPANT), null, "participant.url.not.found", null);
        doReturn(this.resource).when(this.resourceRepository).save(any());
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());
        doReturn(this.getResourceCredentialMock()).when(this.signerService).signResource(anyMap(), any(), anyString());

        Resource resourceActual = this.resourceService.createResource(this.generateMockCreateVirtualDataResourceRequest(), this.randomUUID);
        assertThat(resourceActual.getName()).isEqualTo(this.resource.getName());
    }

    @Test
    void testCreateResource_virtualSoftware_200() {
        doReturn(this.credential).when(this.credentialService).createCredential(anyString(), anyString(), anyString(), anyString(), any());
        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.resource).when(this.resourceRepository).save(any());
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());
        doReturn(this.getResourceCredentialMock()).when(this.signerService).signResource(anyMap(), any(), anyString());

        Resource resourceActual = this.resourceService.createResource(this.generateMockCreateVirtualSoftwareResourceRequest(), this.randomUUID);
        assertThat(resourceActual.getName()).isEqualTo(this.resource.getName());
    }

    @Test
    void testCreateResource_virtualSoftware_subtype_400() {
        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createResourceRequest = this.generateMockCreateVirtualSoftwareResourceRequest();
        createResourceRequest.getCredentialSubject().remove(SUBTYPE);
        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createResourceRequest, this.randomUUID));
    }

    @Test
    void testCreateResource_virtualSoftware_vault_400() {
        Participant participant = this.generateMockParticipant();
        participant.setKeyStored(true);
        doReturn(Optional.of(participant)).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doReturn(StringUtils.EMPTY).when(this.vaultService).getParticipantPrivateKeySecret(anyString());
        CreateResourceRequest createResourceRequest = this.generateMockCreateVirtualSoftwareResourceRequest();
        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createResourceRequest, this.randomUUID));
    }

    @Test
    void testCreateResource_virtualData_legalBasis_400() {

        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createVirtualDataResourceRequest = this.generateMockCreateVirtualDataResourceRequest();
        createVirtualDataResourceRequest.getCredentialSubject().remove(LEGAL_BASIS);

        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createVirtualDataResourceRequest, this.randomUUID));

    }

    @Test
    void testCreateResource_virtualData_piiEmail_400() {

        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createVirtualDataResourceRequest = this.generateMockCreateVirtualDataResourceRequest();
        createVirtualDataResourceRequest.getCredentialSubject().remove(GX_EMAIL);

        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createVirtualDataResourceRequest, this.randomUUID));
    }

    @Test
    void testCreateResource_virtualData_obsoleteDate_400() {

        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createVirtualDataResourceRequest = this.generateMockCreateVirtualDataResourceRequest();
        createVirtualDataResourceRequest.getCredentialSubject().put(OBSOLETE_TIME, "2010-12-01 00:00:00.000");

        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createVirtualDataResourceRequest, this.randomUUID));
    }

    @Test
    void testCreateResource_virtualData_parse_obsoleteDate_400() {

        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createVirtualDataResourceRequest = this.generateMockCreateVirtualDataResourceRequest();
        createVirtualDataResourceRequest.getCredentialSubject().put(OBSOLETE_TIME, this.randomUUID);

        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createVirtualDataResourceRequest, this.randomUUID));
    }

    @Test
    void testCreateResource_virtualData_expiryDate_400() {

        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createVirtualDataResourceRequest = this.generateMockCreateVirtualDataResourceRequest();
        createVirtualDataResourceRequest.getCredentialSubject().put(EXPIRATION_TIME, "2010-12-01 00:00:00.000");

        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createVirtualDataResourceRequest, this.randomUUID));
    }

    @Test
    void testCreateResource_virtualData_parse_expiryDate_400() {

        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doNothing().when(this.signerService).validateRequestUrl(anyList(), anyList(), nullable(String.class), anyString(), nullable(List.class));
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());

        CreateResourceRequest createVirtualDataResourceRequest = this.generateMockCreateVirtualDataResourceRequest();
        createVirtualDataResourceRequest.getCredentialSubject().put(EXPIRATION_TIME, this.randomUUID);

        assertThrows(BadDataException.class, () -> this.resourceService.createResource(createVirtualDataResourceRequest, this.randomUUID));
    }

}