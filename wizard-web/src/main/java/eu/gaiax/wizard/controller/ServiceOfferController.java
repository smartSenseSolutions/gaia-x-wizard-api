package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ServiceAndResourceListDTO;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceIdRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceOfferResponse;
import eu.gaiax.wizard.api.model.service_offer.ServiceOfferingLocationResponse;
import eu.gaiax.wizard.core.service.service_offer.ResourceService;
import eu.gaiax.wizard.core.service.service_offer.ServiceOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ServiceOfferController extends BaseController {

    private final ServiceOfferService serviceOfferService;
    private final ResourceService resourceService;

    @Tag(name = "Service-Offering")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOfferResponse> createServiceOffering(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        //todo email changes remaining get from auth(String) this.requestForClaim("email", principal)
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request, request.getEmail()));
    }

    @Tag(name = "Service-Offering")
    @Operation(summary = "Validate Service offering for enterprise, role = enterprise")
    @PostMapping(path = VALIDATE_SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void validateServiceOfferRequest(@RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        //todo email changes remaining get from auth(String) this.requestForClaim("email", principal)
        this.serviceOfferService.validateServiceOfferMainRequest(request);
    }

    @Tag(name = "Service-Offering")
    @Operation(summary = "Create Service offering for enterprise, role = enterprise")
    @PostMapping(path = PUBLIC_SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<ServiceOfferResponse> createServiceOfferingPublic(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request, null));
    }

    @Tag(name = "Service-Offering")
    @Operation(summary = "Get service locations from policy")
    @PostMapping(path = SERVICE_OFFER_LOCATION, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Object> getServiceOfferingLocation(@Valid @RequestBody ServiceIdRequest serviceIdRequest) {
        ServiceOfferingLocationResponse serviceOfferingLocationResponse = new ServiceOfferingLocationResponse(this.serviceOfferService.getLocationFromService(serviceIdRequest));
        return CommonResponse.of(serviceOfferingLocationResponse);
    }

    @Tag(name = "Service-Offering")
    @Operation(summary = "Get service list for dropdown")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Filter request with sort",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 5,
                                      "sort": {
                                        "column": "name",
                                        "sortType": "ASC"
                                      }
                                    }"""
                    ),
                    @ExampleObject(name = "Filter request with sort and search",
                            value = """
                                    {
                                      "page": 0,
                                      "size": 5,
                                      "sort": {
                                        "column": "name",
                                        "sortType": "ASC"
                                      },
                                      "criteriaOperator": "AND",
                                      "criteria": [
                                        {
                                          "column": "name",
                                          "operator": "CONTAIN",
                                          "values": [
                                            "xyz"
                                          ]
                                        }
                                      ]
                                    }"""
                    ),
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Master data fetched successfully.", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Successful request", value = """
                                    {
                                       "status": 200,
                                       "payload": {
                                         "content": [
                                           {
                                             "id": "0fa1180c-a8bf-4994-8798-66744886acea",
                                             "name": "Storage service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Storage service.json"
                                           },
                                           {
                                             "id": "ab06e01c-978a-459c-9104-1018cb5e1ec9",
                                             "name": "Clould service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Clould_service.json"
                                           },
                                           {
                                             "id": "0749b43f-b187-4a57-8119-8d72ea7dd01f",
                                             "name": "Database service",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/Database_service.json"
                                           },
                                           {
                                             "id": "c6a04238-fa3a-45b9-af1d-1d92872bcaf3",
                                             "name": "Sertvice_offer_1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_QTJG.json"
                                           },
                                           {
                                             "id": "bcb1e1e1-347e-4aa8-ba38-ac35d331e2c6",
                                             "name": "Sertvice_of_1",
                                             "vcUrl": "https://wizard-api.smart-x.smartsenselabs.com/12081064-8878-477e-8092-564a240c69e2/service_6vSP.json"
                                           }
                                         ],
                                         "pageable": {
                                           "pageSize": 5,
                                           "totalPages": 4,
                                           "pageNumber": 0,
                                           "numberOfElements": 5,
                                           "totalElements": 18
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(examples = {})
            }),
    })
    @PostMapping(path = SERVICE_OFFER_LIST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ServiceAndResourceListDTO>> getServiceOfferingLList(@Valid @RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.serviceOfferService.getServiceOfferingList(filterRequest));
    }


}
