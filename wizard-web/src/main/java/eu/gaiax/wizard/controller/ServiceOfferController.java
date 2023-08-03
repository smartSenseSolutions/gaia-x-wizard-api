package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.ServiceOffer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.ServiceOffer.ResourceRequest;
import eu.gaiax.wizard.core.service.ServiceOffer.ResourceService;
import eu.gaiax.wizard.core.service.ServiceOffer.ServiceOfferService;
import eu.gaiax.wizard.dao.entity.serviceoffer.ServiceOffer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

import static eu.gaiax.wizard.api.model.StringPool.GAIA_X_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = GAIA_X_BASE_PATH)
@RequiredArgsConstructor
public class ServiceOfferController extends BaseResource {

    private final ServiceOfferService serviceOfferService;
    private final ResourceService resourceService;

    @Tag(name = "Service-Offering")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = "/service-offers", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOffer> createServiceOffering(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request,"mittal.vaghela+1@smartsensesolutions.com"));
    }
/*    @Tag(name = "Service-Offering")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = "/public-api/service-offers", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOffer> createServiceOfferingPublic(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request,null));
    }*/
    @Tag(name = "Resources")
    @Operation(summary = "Create Resource")
    @PostMapping(path = "/resource", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOffer> createResource(@Valid @RequestBody ResourceRequest request, Principal principal) throws IOException {
        return CommonResponse.of(this.resourceService.createResource(request,"mittal.vaghela@smartsensesolutions.com"));
    }

}
