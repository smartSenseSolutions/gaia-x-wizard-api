package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.ServiceOffer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.core.service.ServiceOffer.ServiceOfferService;
import eu.gaiax.wizard.dao.entity.serviceoffer.ServiceOffer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ServiceOfferController {

    private final ServiceOfferService serviceOfferService;

    @Tag(name = "Catalogue")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = SERVICE_OFFERING, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOffer> createServiceOffering(@Valid @RequestBody CreateServiceOfferingRequest request) throws IOException {
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request,"email"));
    }
}
