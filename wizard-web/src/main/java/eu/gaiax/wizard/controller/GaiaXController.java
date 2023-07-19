/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.RegisterRequest;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.enterprise.EnterpriseService;
import eu.gaiax.wizard.core.service.enterprise.RegistrationService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.entity.EnterpriseCredential;
import eu.gaiax.wizard.dao.entity.ServiceOffer;
import eu.gaiax.wizard.dao.entity.ServiceOfferView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class GaiaXController extends BaseResource {

    private final RegistrationService registrationService;

    private final DomainService domainService;

    private final CertificateService certificateService;

    private final K8SService k8SService;

    private final EnterpriseService enterpriseService;

    private final SignerService signerService;

    private final CredentialService credentialService;

    @Operation(summary = "Get .well-known files, this is public API")
    @GetMapping(path = GET_ENTERPRISE_FILES)
    @Tag(name = "Well-known")
    public String getEnterpriseFiles(@PathVariable(name = "fileName") String fileName, @RequestHeader(name = HttpHeaders.HOST) String host) throws IOException {
        return this.enterpriseService.getEnterpriseFiles(host, fileName);
    }

    @Operation(summary = "Register enterprise in the system. This will save enterprise data in database and create job to create subdomain")
    @PostMapping(path = REGISTER, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Tag(name = "Onboarding")
    public CommonResponse<Enterprise> registerBusiness(@RequestBody @Valid RegisterRequest registerRequest) throws SchedulerException {
        return CommonResponse.of(this.registrationService.registerEnterprise(registerRequest));
    }


    @Tag(name = "Enterprise")
    @Operation(summary = "get all enterprises, pagination, search and sort will be added, role: Admin")
    @GetMapping(path = ENTERPRISE_LIST, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<List<Enterprise>> listEnterprise() {
        return CommonResponse.of(this.enterpriseService.listEnterprise());
    }

    @Tag(name = "Enterprise")
    @Operation(summary = "get logged in user's enterprises, role: Enterprise")
    @GetMapping(path = ENTERPRISE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Enterprise> getEnterpriseDetails(Principal principal) {
        return CommonResponse.of(this.enterpriseService.getEnterprise(this.getEnterpriseId(principal)));
    }


    @Tag(name = "Enterprise")
    @Operation(summary = "Get enterprise by id, role admin")
    @GetMapping(path = ENTERPRISE_BY_ID, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Enterprise> getEnterprise(@PathVariable(name = "id") long enterpriseId) {
        return CommonResponse.of(this.enterpriseService.getEnterprise(enterpriseId));
    }


    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from sub domain creation")
    @GetMapping(path = CREATE_SUBDOMAIN)
    public CommonResponse<Map<String, String>> createSubDomain(@PathVariable(name = "enterpriseId") long enterpriseId) {
        this.domainService.createSubDomain(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Subdomain creation started");
        return CommonResponse.of(map);
    }

    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from SLL certificate creation")
    @GetMapping(path = CREATE_CERTIFICATE)
    public CommonResponse<Map<String, String>> createCertificate(@PathVariable(name = "enterpriseId") long enterpriseId) {
        this.certificateService.createSSLCertificate(enterpriseId, null);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Certification creation started");
        return CommonResponse.of(map);
    }


    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from ingress creation")
    @GetMapping(path = CREATE_INGRESS)
    public CommonResponse<Map<String, String>> createIngress(@PathVariable(name = "enterpriseId") long enterpriseId) {
        this.k8SService.createIngress(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Ingress creation started");
        return CommonResponse.of(map);
    }

    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from did creation")
    @GetMapping(path = CREATE_DID)
    public CommonResponse<Map<String, String>> createDid(@PathVariable(name = "enterpriseId") long enterpriseId) {
        this.signerService.createDid(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "did creation started");
        return CommonResponse.of(map);
    }

    @Tag(name = "Onboarding")
    @Operation(summary = "Resume onboarding process from participant credential creation")
    @GetMapping(path = CREATE_PARTICIPANT_JSON)
    public CommonResponse<Map<String, String>> createParticipantJson(@PathVariable(name = "enterpriseId") long enterpriseId) {
        this.signerService.createParticipantJson(enterpriseId);
        Map<String, String> map = new HashMap<>();
        map.put("message", "participant json creation started");
        return CommonResponse.of(map);
    }

    @Tag(name = "Credentials")
    @Operation(summary = "Get all issued VC of enterprise, role = Enterprise")
    @GetMapping(path = ENTERPRISE_VC)
    public CommonResponse<List<EnterpriseCredential>> getEnterpriseCredentials(Principal principal) {
        return CommonResponse.of(this.enterpriseService.getEnterpriseCredentials(this.getEnterpriseId(principal)));
    }

    @Tag(name = "Catalogue")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = SERVICE_OFFERING, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOffer> createServiceOffering(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        return CommonResponse.of(this.enterpriseService.createServiceOffering(this.getEnterpriseId(principal), request));
    }

    @Tag(name = "Catalogue")
    @Operation(summary = "Get service offering of enterprise, role enterprise")
    @GetMapping(path = SERVICE_OFFERING, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<List<ServiceOfferView>> serviceOfferList(Principal principal) {
        return CommonResponse.of(this.enterpriseService.serviceOfferList(this.getEnterpriseId(principal)));
    }

    @Tag(name = "Catalogue")
    @Operation(summary = "Get service offering by id of enterprise, details with meta information, role enterprise")
    @GetMapping(path = SERVICE_OFFER_BY_ID, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOfferView> getServiceOfferDetailsById(@PathVariable(name = "id") long id, Principal principal) {
        return CommonResponse.of(this.enterpriseService.getServiceOfferDetailsById(this.getEnterpriseId(principal), id));
    }

    @Tag(name = "Catalogue")
    @Operation(summary = "List all service offering: Pagination, search and sort wil be added, role = enterprise")
    @GetMapping(path = CATALOGUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<List<ServiceOfferView>> getAllServiceOffers(Principal principal) {
        return CommonResponse.of(this.enterpriseService.allServiceOfferList(this.getEnterpriseId(principal)));
    }


    @Tag(name = "Credentials")
    @Operation(summary = "Create/Get VP of Gaia-x participant of any credential, role = enterprise")
    @GetMapping(path = CREATE_VP, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> createVP(@PathVariable(name = "name") String name, Principal principal) throws JsonProcessingException {
        return CommonResponse.of(this.credentialService.createVP(this.getEnterpriseId(principal), name));
    }

    @Tag(name = "Catalogue")
    @Operation(summary = "Get Service offer details. This API will consume participant VP and if VP is valid then it will return service  offering details, role = enterprise")
    @PostMapping(path = SERVICE_OFFER_DETAILS, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> serviceOfferDetails(@PathVariable(name = "offerId") long offerId,
                                                                   @RequestBody Map<String, Object> vp) {
        return CommonResponse.of(this.enterpriseService.serviceOfferDetails(offerId, vp));
    }

    @Tag(name = "Enterprise")
    @Operation(summary = "export private keys. it will return s3 pre-signed URLs")
    @GetMapping(path = EXPORT_KEYS, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> exportKeys(Principal principal) {
        return CommonResponse.of(this.enterpriseService.exportKeys(this.getEnterpriseId(principal)));
    }
}
