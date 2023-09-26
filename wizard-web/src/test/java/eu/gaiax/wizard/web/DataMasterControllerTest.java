package eu.gaiax.wizard.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.GaiaXWizardApplication;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.dao.repository.data_master.*;
import eu.gaiax.wizard.util.ContainerContextInitializer;
import eu.gaiax.wizard.util.HelperService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;

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

    private void request_master_data(Long count, String type) {
        FilterRequest request = HelperService.prepareDefaultFilterRequest();
        ResponseEntity<CommonResponse> response = this.restTemplate.exchange("/public/master-data/" + type + "/filter", HttpMethod.POST, new HttpEntity<>(request), CommonResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageResponse pageResponse = this.mapper.convertValue(response.getBody().getPayload(), PageResponse.class);
        assertEquals(count, pageResponse.getPageable().getTotalElements());
        assertTrue(((Collection<?>) pageResponse.getContent()).size() > 0);
    }
}
