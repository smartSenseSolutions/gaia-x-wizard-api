package eu.gaiax.wizard.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.LabelLevelTypeInterface;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.service_offer.ServiceIdRequest;
import eu.gaiax.wizard.core.service.data_master.LabelLevelService;
import eu.gaiax.wizard.core.service.service_offer.PolicyService;
import eu.gaiax.wizard.dao.repository.data_master.*;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import eu.gaiax.wizard.util.HelperService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {GaiaXWizardApplication.class})
@ActiveProfiles("test")
@ContextConfiguration(initializers = {ContainerContextInitializer.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataMasterControllerTest {

    @Autowired
    private AccessTypeMasterRepository accessTypeMasterRepository;
    @Autowired
    private EntityTypeMasterRepository entityTypeMasterRepository;
    @Autowired
    private FormatTypeMasterRepository formatTypeMasterRepository;
    @Autowired
    private RegistrationTypeMasterRepository registrationTypeMasterRepository;
    @Autowired
    private RequestTypeMasterRepository requestTypeMasterRepository;
    @Autowired
    private StandardTypeMasterRepository standardTypeMasterRepository;
    @Autowired
    private SubdivisionCodeMasterRepository subdivisionCodeMasterRepository;
    @Autowired
    private SpdxLicenseMasterRepository spdxLicenseMasterRepository;
    @Autowired
    private LabelLevelService labelLevelService;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    @Autowired
    private PolicyService policyService;

    @Test
    void fetch_access_master_200() {
        final String type = "access";
        this.request_master_data(this.accessTypeMasterRepository.count(), type);
    }

    @Test
    void fetch_entity_master_200() {
        final String type = "entity";
        this.request_master_data(this.entityTypeMasterRepository.count(), type);
    }

    @Test
    void fetch_format_master_200() {
        final String type = "format";
        this.request_master_data(this.formatTypeMasterRepository.count(), type);
    }

    @Test
    void fetch_registration_master_200() {
        final String type = "registration";
        this.request_master_data(this.registrationTypeMasterRepository.count(), type);
    }

    @Test
    void fetch_request_master_200() {
        final String type = "request";
        this.request_master_data(this.requestTypeMasterRepository.count(), type);
    }

    @Test
    void fetch_standard_master_200() {
        final String type = "standard";
        this.request_master_data(this.standardTypeMasterRepository.count(), type);
    }

    @Test
    void fetch_subdivision_master_200() {
        final String type = "subdivision";
        this.request_master_data(this.subdivisionCodeMasterRepository.count(), type);
    }

    @Test
    void fetch_spdx_master_200() {
        final String type = "spdxLicense";
        this.request_master_data(this.spdxLicenseMasterRepository.count(), type);
    }

    private void request_master_data(Long count, String type) {
        FilterRequest request = HelperService.prepareDefaultFilterRequest();
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange("/public/master-data/" + type + "/filter", HttpMethod.POST, new HttpEntity<>(request), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageResponse pageResponse = this.mapper.convertValue(response.getBody().getPayload(), PageResponse.class);
        assertEquals(count, pageResponse.getPageable().getTotalElements());
        assertTrue(((Collection<?>) pageResponse.getContent()).size() > 0);
    }

    @Test
    void fetch_subdivision_name_200() {
        final String type = "subdivisionMaster";
        doReturn(new String[]{"BE-BRU"}).when(this.policyService).getLocationByServiceOfferingId(anyString());
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange("/public/service-offer/location", HttpMethod.POST, new HttpEntity<>(new ServiceIdRequest(UUID.randomUUID().toString())), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> payload = (Map<String, Object>) response.getBody().getPayload();
        assertEquals(Collections.singletonList("Brussels Hoofdstedelijk Gewest"), payload.get("serviceAvailabilityLocation"));
    }

    @Test
    void fetch_subdivision_master_400() {
        final String type = "subdivisionMaster";
        FilterRequest request = HelperService.prepareDefaultFilterRequest();
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange("/public/master-data/" + type + "/filter", HttpMethod.POST, new HttpEntity<>(request), CommonResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void fetch_label_level_list_200() {
        List<LabelLevelTypeInterface> labelLevelTypeAndQuestionList = this.labelLevelService.getLabelLevelTypeAndQuestionList();
        assertThat(labelLevelTypeAndQuestionList).isNotNull();
    }

}
