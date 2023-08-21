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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create Service offer", value = """
                            {
                              "name": "name of service",
                              "verificationMethod": "did:web:exmaple.com",
                              "email": "casio50@exmaple.com",
                              "description": "test service data",
                              "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                              "credentialSubject": {
                                "gx:termsAndConditions": {
                                  "gx:URL": "https://aws.amazon.com/service-terms/"
                                },
                                "gx:policy": {
                                  "gx:location": [
                                    "BE-BRU"
                                  ]
                                },
                                "gx:dataAccountExport": {
                                  "gx:requestType": "API",
                                  "gx:accessType": "physical",
                                  "gx:formatType": "pdf"
                                },
                                "gx:aggregationOf": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                  }
                                ],
                                "gx:dependsOn": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/service_S7GZ.json"
                                  }
                                ],
                                "gx:dataProtectionRegime": "GDPR2016",
                                "type": "gx:ServiceOffering"
                              }
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                        "id": "785651fd-8292-4555-b05c-77c03cea2312",
                                                        "email": "participant@example.in",
                                                        "legalName": "Participant Example",
                                                        "shortName": "ParticipantShortName",
                                                        "entityType": {
                                                          "id": "933fde99-d815-4d10-b414-ea2d88d10474",
                                                          "type": "Social Enterprise",
                                                          "active": true
                                                        },
                                                        "domain": "ParticipantShortName.smart-x.smartsenselabs.com",
                                                        "participantType": "REGISTERED",
                                                        "status": 0,
                                                        "credential": "{\\"legalParticipant\\":{\\"credentialSubject\\":{\\"gx:legalName\\":\\"Participant Example\\",\\"gx:headquarterAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"},\\"gx:legalAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"}}},\\"legalRegistrationNumber\\":{\\"gx:leiCode\\":\\"9695007586XZAKPYJ703\\"}}",
                                                        "ownDidSolution": false
                                                      }
                                                    }                                                                                                        
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid (Participant,Dependence On,Aggregation of )",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "message": "Validation has been failed.",
                                              "status": 400,
                                              "payload": {
                                                "error": {
                                                  "message": "Validation has been failed.",
                                                  "status": 400,
                                                  "timeStamp": 1692342059774
                                                }
                                              }
                                            }
                                            """)
                            })
                    }),
    })
    public CommonResponse<ServiceOfferResponse> createServiceOffering(@Valid @RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        //todo email changes remaining get from auth(String) this.requestForClaim("email", principal)
        return CommonResponse.of(this.serviceOfferService.createServiceOffering(request, request.getEmail()));
    }

    @Tag(name = "Service-Offering")
    @Operation(summary = "Validate Service offering for enterprise, role = enterprise")
    @PostMapping(path = VALIDATE_SERVICE_OFFER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create Service offer", value = """
                            {
                              "name": "name of service",
                              "description": "test service data",
                              "credentialSubject": {
                                "gx:termsAndConditions": {
                                  "gx:URL": "https://aws.amazon.com/service-terms/"
                                },
                                "gx:policy": {
                                  "gx:location": [
                                    "BE-BRU"
                                  ]
                                },
                                "gx:dataAccountExport": {
                                  "gx:requestType": "API",
                                  "gx:accessType": "physical",
                                  "gx:formatType": "pdf"
                                },
                                "gx:aggregationOf": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                  }
                                ],
                                "gx:dependsOn": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/service_S7GZ.json"
                                  }
                                ],
                                "gx:dataProtectionRegime": "GDPR2016",
                                "type": "gx:ServiceOffering"
                              }
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                    }                                                                                                        
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid (Participant,Dependence On,Aggregation of )",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "message": "Validation has been failed.",
                                              "status": 400,
                                              "payload": {
                                                "error": {
                                                  "message": "Validation has been failed.",
                                                  "status": 400,
                                                  "timeStamp": 1692342059774
                                                }
                                              }
                                            }
                                            """)
                            })
                    }),
    })
    public void validateServiceOfferRequest(@RequestBody CreateServiceOfferingRequest request, Principal principal) throws IOException {
        //todo email changes remaining get from auth(String) this.requestForClaim("email", principal)
        this.serviceOfferService.validateServiceOfferMainRequest(request);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Create Service offer", value = """
                            {
                              "name": "name of service",
                              "verificationMethod": "did:web:exmaple.com",
                              "participantJsonUrl": "https://example.com/12081064-8878-477e-8092-564a240c69e2/participant.json",
                              "description": "test service data",
                              "privateKey": "-----BEGIN PRIVATE KEY---  ----END PRIVATE KEY-----",
                              "credentialSubject": {
                                "gx:termsAndConditions": {
                                  "gx:URL": "https://aws.amazon.com/service-terms/"
                                },
                                "gx:policy": {
                                  "gx:location": [
                                    "BE-BRU"
                                  ]
                                },
                                "gx:dataAccountExport": {
                                  "gx:requestType": "API",
                                  "gx:accessType": "physical",
                                  "gx:formatType": "pdf"
                                },
                                "gx:aggregationOf": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/resource_9a40cafd-43ed-41b0-a53e-4e2af164fde5.json"
                                  }
                                ],
                                "gx:dependsOn": [
                                  {
                                    "id": "https://exmaple.com/12081064-8878-477e-8092-564a240c69e2/service_S7GZ.json"
                                  }
                                ],
                                "gx:dataProtectionRegime": "GDPR2016",
                                "type": "gx:ServiceOffering"
                              }
                            }
                            """)
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service offering successfully.",
                    content = {
                            @Content(
                                    examples = {
                                            @ExampleObject(name = "Success Response", value = """
                                                    {
                                                      "status": 200,
                                                      "payload": {
                                                        "id": "785651fd-8292-4555-b05c-77c03cea2312",
                                                        "email": "participant@example.in",
                                                        "legalName": "Participant Example",
                                                        "shortName": "ParticipantShortName",
                                                        "entityType": {
                                                          "id": "933fde99-d815-4d10-b414-ea2d88d10474",
                                                          "type": "Social Enterprise",
                                                          "active": true
                                                        },
                                                        "domain": "ParticipantShortName.smart-x.smartsenselabs.com",
                                                        "participantType": "REGISTERED",
                                                        "status": 0,
                                                        "credential": "{\\"legalParticipant\\":{\\"credentialSubject\\":{\\"gx:legalName\\":\\"Participant Example\\",\\"gx:headquarterAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"},\\"gx:legalAddress\\":{\\"gx:countrySubdivisionCode\\":\\"BE-BRU\\"}}},\\"legalRegistrationNumber\\":{\\"gx:leiCode\\":\\"9695007586XZAKPYJ703\\"}}",
                                                        "ownDidSolution": false
                                                      }
                                                    }                                                                                                        
                                                    """)
                                    }
                            )
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid (Participant,Dependence On,Aggregation of )",
                    content = {
                            @Content(examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "message": "Validation has been failed.",
                                              "status": 400,
                                              "payload": {
                                                "error": {
                                                  "message": "Validation has been failed.",
                                                  "status": 400,
                                                  "timeStamp": 1692342059774
                                                }
                                              }
                                            }
                                            """)
                            })
                    }),
    })
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
    @PostMapping(path = SERVICE_OFFER_LIST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ServiceAndResourceListDTO>> getServiceOfferingLList(@Valid @RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.serviceOfferService.getServiceOfferingList(filterRequest));
    }


}
