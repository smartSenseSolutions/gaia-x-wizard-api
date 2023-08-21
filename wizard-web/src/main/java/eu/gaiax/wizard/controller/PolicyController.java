package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.core.service.service_offer.PolicyService;
import eu.gaiax.wizard.utils.WizardRestConstant;
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

import static eu.gaiax.wizard.utils.WizardRestConstant.PUBLIC_POLICY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class PolicyController extends BaseController {

    private final PolicyService policyService;

    @Tag(name = "Policy")
    @Operation(summary = "Create Policy")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(examples = {
                    @ExampleObject(name = "Filter request with sort and without search", value = """
                            {
                               "rightOperand": [
                                 "BE-BRU"
                               ],
                               "leftOperand": "verifiableCredential.credentialSubject.legalAddress.country",
                               "target": "did:web:data.smart-x.smartsenselabs.com",
                               "assigner": "did:web:admin.smart-x.smartsenselabs.com",
                               "domain": "smart5",
                               "serviceName": "service001"
                            }"""
                    )
            })
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy JSON created successfully.", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Successful request", value = """
                                    {
                                       "status": 200,
                                       "payload": {
                                         "content": [
                                           {
                                             "id": "0c9b529a-94ef-4ea7-8aa8-ed51780b16ba",
                                             "type": "application/1d-interleaved-parityfec",
                                             "active": true
                                           },
                                           {
                                             "id": "4821bd62-491f-4efc-b95a-08410a4ddbab",
                                             "type": "application/3gpdash-qoe-report+xml",
                                             "active": true
                                           },
                                           {
                                             "id": "736df04f-2fcd-41e9-9a69-a4811ac194d7",
                                             "type": "application/3gppHalForms+json",
                                             "active": true
                                           },
                                           {
                                             "id": "3b64f941-29e7-402c-b871-82ce5b6d05c2",
                                             "type": "application/3gppHal+json",
                                             "active": true
                                           },
                                           {
                                             "id": "961b359b-47b1-4524-8fdc-13c83ac11baa",
                                             "type": "application/3gpp-ims+xml",
                                             "active": true
                                           }
                                         ],
                                         "pageable": {
                                           "pageSize": 5,
                                           "totalPages": 40,
                                           "pageNumber": 0,
                                           "numberOfElements": 5,
                                           "totalElements": 200,
                                           "sort": {
                                             "column": "type",
                                             "sortType": "ASC"
                                           }
                                         }
                                       }
                                    }"""
                            )
                    })
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = {
                    @Content(examples = {
                            @ExampleObject(name = "Invalid data type", value = """
                                    {
                                      "message": "Master data not found with name 'entities'",
                                      "status": 400,
                                      "payload": {
                                        "error": {
                                          "message": "Master data not found with name 'entities'",
                                          "status": 400,
                                          "timeStamp": 1692356630682
                                        }
                                      }
                                    }"""
                            )
                    })
            }),
    })
    @PostMapping(path = PUBLIC_POLICY, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Object> createODRLPolicy(@Valid @RequestBody ODRLPolicyRequest odrlPolicyRequest) throws IOException {
        return CommonResponse.of(this.policyService.createPolicy(odrlPolicyRequest, null));
    }

    @Tag(name = "Policy")
    @Operation(summary = "Policy evaluator for catalogue")
    @PostMapping(path = WizardRestConstant.POLICY_EVALUATE, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<JsonNode> evaluatePolicy(@Valid @RequestBody PolicyEvaluationRequest policyEvaluationRequest) {
        return CommonResponse.of(this.policyService.evaluatePolicy(policyEvaluationRequest));
    }
}
