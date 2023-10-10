package eu.gaiax.wizard.core.service.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ResourceFilterResponse;
import eu.gaiax.wizard.api.model.ResourceType;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.service_offer.PolicyService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.resource.ResourceRepository;
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
import static org.mockito.ArgumentMatchers.*;
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
    private ResourceService resourceService;
    private Resource resource;
    private Credential credential;

    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.objectMapper = this.configureObjectMapper();
        ContextConfig contextConfig = new ContextConfig(null, null, null, null, null, List.of("http://www.w3.org/ns/odrl.jsonld", "https://www.w3.org/ns/odrl/2/ODRL22.json"), List.of("https://www.w3.org/2018/credentials/v1", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"));
        this.resourceService = Mockito.spy(new ResourceService(this.resourceRepository, this.participantService, null, this.participantRepository, contextConfig,
                this.objectMapper, this.credentialService, null, this.signerService, null, null, this.policyService));
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
    void testCreateResource() {
        doReturn(this.credential).when(this.credentialService).createCredential(anyString(), anyString(), anyString(), anyString(), any());
        doReturn(Optional.of(this.generateMockParticipant())).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        Mockito.doNothing().when(this.signerService).validateRequestUrl(Collections.singletonList(this.randomUUID), List.of(GX_LEGAL_PARTICIPANT), null, "participant.url.not.found", null);
        doReturn(this.resource).when(this.resourceRepository).save(any());
        doReturn(this.credential).when(this.credentialService).getByParticipantWithCredentialType(any(), anyString());
        doReturn(this.getResourceCredentialMock()).when(this.signerService).signResource(anyMap(), any(), anyString());
        Resource resourceActual = this.resourceService.createResource(this.generateMockCreateResourceRequest(), this.randomUUID);

        assertThat(resourceActual.getName()).isEqualTo(this.resource.getName());
    }

    @Test
    void testFilterResource() {

        doReturn(new PageImpl<>(Collections.singletonList(this.resource))).when(this.resourceService).filter(any());
        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setPage(0);
        filterRequest.setSize(1);
        PageResponse<ResourceFilterResponse> resourceFilterPageResponse = this.resourceService.filterResource(filterRequest, null);

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

    private CreateResourceRequest generateMockCreateResourceRequest() {
        CreateResourceRequest createResourceRequest = new CreateResourceRequest();
        createResourceRequest.setStoreVault(false);
        createResourceRequest.setPrivateKey(this.randomUUID);

        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put(TYPE, ResourceType.PHYSICAL_RESOURCE.getValue());
        credentialSubject.put(NAME, this.randomUUID);

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

}