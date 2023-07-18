/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.LoginRequest;
import eu.gaiax.wizard.api.model.LoginResponse;
import eu.gaiax.wizard.api.model.RegisterRequest;
import eu.gaiax.wizard.api.model.SessionDTO;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.enterprise.EnterpriseService;
import eu.gaiax.wizard.core.service.enterprise.RegistrationService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.keycloak.KeycloakService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.entity.EnterpriseCredential;
import eu.gaiax.wizard.dao.entity.ServiceOffer;
import eu.gaiax.wizard.dao.entity.ServiceOfferView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type Gaia x controller.
 *
 * @author Nitin
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
public class GaiaXController {

    private final RegistrationService registrationService;

    private final DomainService domainService;

    private final CertificateService certificateService;

    private final K8SService k8SService;

    private final EnterpriseService enterpriseService;

    private final SignerService signerService;

    private final CredentialService credentialService;


    private void validateAccess(Set<Integer> requiredRoles, int userRole) {
        boolean contains = requiredRoles.contains(userRole);
        Validate.isFalse(contains).launch(new SecurityException("can not access API"));
    }

    /**
     * Register business enterprise.
     *
     * @param registerRequest the register request
     * @return the enterprise
     * @throws SchedulerException the scheduler exception
     */
    @Tag(name = "Login")
//    @Operation(summary = "Login(type=1 for login as admin, type =2 for login as enterprise)")
    @Operation(summary = "Login(type=1 for login as admin)")
    @PostMapping(path = "login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<LoginResponse> login(@RequestBody @Valid LoginRequest registerRequest) {
        return CommonResponse.of(enterpriseService.login(registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getType()));
    }

    /**
     * Gets enterprise files.
     *
     * @param fileName the file name
     * @param host     the host
     * @return the enterprise files
     * @throws IOException the io exception
     */
    @Operation(summary = "Get .well-known files, this is public API")
    @GetMapping(path = ".well-known/{fileName}")
    @Tag(name = "Well-known")
    public String getEnterpriseFiles(@PathVariable(name = "fileName") String fileName, @RequestHeader(name = HttpHeaders.HOST) String host) throws IOException {
        return enterpriseService.getEnterpriseFiles(host, fileName);
    }

    /**
     * Register business enterprise.
     *
     * @param registerRequest the register request
     * @return the enterprise
     * @throws SchedulerException the scheduler exception
     */
    @Operation(summary = "Register enterprise in the system. This will save enterprise data in database and create job to create subdomain")
    @PostMapping(path = "register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Onboarding")
    public CommonResponse<Enterprise> registerBusiness(@RequestBody @Valid RegisterRequest registerRequest) throws SchedulerException {
        return CommonResponse.of(registrationService.registerEnterprise(registerRequest));
    }


    /**
     * List enterprise common response.
     *
     * @param sessionDTO the session dto
     * @return the common response
     * @throws SchedulerException the scheduler exception
     */
    @Tag(name = "Enterprise")
    @Operation(summary = "get all enterprises, pagination, search and sort will be added, role: Admin")
    @GetMapping(path = "enterprises/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<Enterprise>> listEnterprise(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) {
        validateAccess(Set.of(StringPool.ADMIN_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.listEnterprise());
    }

    /**
     * Gets enterprise details.
     *
     * @param sessionDTO the session dto
     * @return the enterprise details
     */
    @Tag(name = "Enterprise")
    @Operation(summary = "get logged in user's enterprises, role: Enterprise")
    @GetMapping(path = "enterprises", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Enterprise> getEnterpriseDetails(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.getEnterprise(sessionDTO.getEnterpriseId()));
    }


    /**
     * Gets enterprise.
     *
     * @param enterpriseId the enterprise id
     * @param sessionDTO   the session dto
     * @return the enterprise
     * @throws SchedulerException the scheduler exception
     */
    @Tag(name = "Enterprise")
    @Operation(summary = "Get enterprise by id, role admin")
    @GetMapping(path = "enterprises/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Enterprise> getEnterprise(@PathVariable(name = "id") long enterpriseId, @Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) {
        validateAccess(Set.of(StringPool.ADMIN_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.getEnterprise(enterpriseId));
    }


    /**
     * Create sub domain string.
     *
     * @param enterpriseId the enterprise id
     * @return the string
     */
    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from sub domain creation")
    @GetMapping(path = "subdomain/{enterpriseId}")
    public CommonResponse<Map<String, String>> createSubDomain(@PathVariable(name = "enterpriseId") long enterpriseId) {
        domainService.createSubDomain(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Subdomain creation started");
        return CommonResponse.of(map);
    }

    /**
     * Create certificate string.
     *
     * @param enterpriseId the enterprise id
     * @return the string
     */
    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from SLL certificate creation")
    @GetMapping(path = "certificate/{enterpriseId}")
    public CommonResponse<Map<String, String>> createCertificate(@PathVariable(name = "enterpriseId") long enterpriseId) {
        certificateService.createSSLCertificate(enterpriseId, null);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Certification creation started");
        return CommonResponse.of(map);
    }


    /**
     * Create ingress string.
     *
     * @param enterpriseId the enterprise id
     * @return the string
     */
    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from ingress creation")
    @GetMapping(path = "ingress/{enterpriseId}")
    public CommonResponse<Map<String, String>> createIngress(@PathVariable(name = "enterpriseId") long enterpriseId) {
        k8SService.createIngress(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Ingress creation started");
        return CommonResponse.of(map);
    }

    /**
     * Create did string.
     *
     * @param enterpriseId the enterprise id
     * @return the string
     */
    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from did creation")
    @GetMapping(path = "did/{enterpriseId}")
    public CommonResponse<Map<String, String>> createDid(@PathVariable(name = "enterpriseId") long enterpriseId) {
        signerService.createDid(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "did creation started");
        return CommonResponse.of(map);
    }

    /**
     * Create participant json string.
     *
     * @param enterpriseId the enterprise id
     * @return the string
     */
    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from participant credential creation")
    @GetMapping(path = "participant/{enterpriseId}")
    public CommonResponse<Map<String, String>> createParticipantJson(@PathVariable(name = "enterpriseId") long enterpriseId) {
        signerService.createParticipantJson(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "participant json creation started");
        return CommonResponse.of(map);
    }

    /**
     * Gets enterprise credentials.
     *
     * @param sessionDTO the session dto
     * @return the enterprise credentials
     */
    @Tag(name = "Credentials")
    @Operation(summary = "Get all issued VC of enterprise, role = Enterprise")
    @GetMapping(path = "enterprises/vcs")
    public CommonResponse<List<EnterpriseCredential>> getEnterpriseCredentials(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.getEnterpriseCredentials(sessionDTO.getEnterpriseId()));
    }

    /**
     * Create service offering common response.
     *
     * @param sessionDTO the session dto
     * @param request    the request
     * @return the common response
     * @throws IOException the io exception
     */
    @Tag(name = "Catalogue")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = "enterprises/service-offers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOffer> createServiceOffering(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO,
                                                              @Valid @RequestBody CreateServiceOfferingRequest request
    ) throws IOException {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.createServiceOffering(sessionDTO.getEnterpriseId(), request));
    }

    /**
     * Create service offering common response.
     *
     * @param sessionDTO the session dto
     * @return the common response
     */
    @Tag(name = "Catalogue")
    @Operation(summary = "Get service offering of enterprise, role enterprise")
    @GetMapping(path = "enterprises/service-offers", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<ServiceOfferView>> serviceOfferList(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.serviceOfferList(sessionDTO.getEnterpriseId()));
    }

    /**
     * Create service offering common response.
     *
     * @param sessionDTO the session dto
     * @param id         the id
     * @return the common response
     */
    @Tag(name = "Catalogue")
    @Operation(summary = "Get service offering by id of enterprise, details with meta information, role enterprise")
    @GetMapping(path = "enterprises/service-offers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOfferView> getServiceOfferDetailsById(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO,
                                                                       @PathVariable(name = "id") long id) {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.getServiceOfferDetailsById(sessionDTO.getEnterpriseId(), id));
    }

    /**
     * Gets all service offers.
     *
     * @param sessionDTO the session dto
     * @return the all service offers
     */
    @Tag(name = "Catalogue")
    @Operation(summary = "List all service offering: Pagination, search and sort wil be added, role = enterprise")
    @GetMapping(path = "catalogue", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<ServiceOfferView>> getAllServiceOffers(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.allServiceOfferList(sessionDTO.getEnterpriseId()));
    }


    /**
     * Create VP
     *
     * @param sessionDTO the session dto
     * @param name       the name
     * @return the all service offers
     * @throws JsonProcessingException the json processing exception
     */
    @Tag(name = "Credentials")
    @Operation(summary = "Create/Get VP of Gaia-x participant of any credential, role = enterprise")
    @GetMapping(path = "enterprises/vc/{name}/vp", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> createVP(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO, @PathVariable(name = "name") String name) throws JsonProcessingException {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(credentialService.createVP(sessionDTO.getEnterpriseId(), name));
    }

    /**
     * Service offer details common response.
     *
     * @param sessionDTO the session dto
     * @param offerId    the offer id
     * @param vp         the vp
     * @return the common response
     * @throws JsonProcessingException the json processing exception
     */
    @Tag(name = "Catalogue")
    @Operation(summary = "Get Service offer details. This API will consume participant VP and if VP is valid then it will return service  offering details, role = enterprise")
    @PostMapping(path = "enterprises/service-offers/{offerId}/details", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> serviceOfferDetails(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO,
                                                                   @PathVariable(name = "offerId") long offerId,
                                                                   @RequestBody Map<String, Object> vp) throws JsonProcessingException {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.serviceOfferDetails(offerId, vp));
    }

    /**
     * Export keys common response.
     *
     * @param sessionDTO the session dto
     * @return the common response
     * @throws JsonProcessingException the json processing exception
     */
    @Tag(name = "Enterprise")
    @Operation(summary = "export private keys. it will return s3 pre-signed URLs")
    @GetMapping(path = "enterprises/keys/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> exportKeys(@Parameter(hidden = true) @RequestAttribute(value = StringPool.SESSION_DTO) SessionDTO sessionDTO) throws JsonProcessingException {
        validateAccess(Set.of(StringPool.ENTERPRISE_ROLE), sessionDTO.getRole());
        return CommonResponse.of(enterpriseService.exportKeys(sessionDTO.getEnterpriseId()));
    }
}
