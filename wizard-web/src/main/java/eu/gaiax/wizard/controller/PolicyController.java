package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.core.service.service_offer.PolicyService;
import eu.gaiax.wizard.utils.WizardRestConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static eu.gaiax.wizard.utils.WizardRestConstant.PUBLIC_POLICY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@Tag(name = "Policy")
public class PolicyController extends BaseController {

    private final PolicyService policyService;

    @Tag(name = "Policy")
    @Operation(summary = "Create Policy")
    @PostMapping(path = PUBLIC_POLICY, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> createODRLPolicy(@Valid @RequestBody ODRLPolicyRequest odrlPolicyRequest) throws IOException {
        return CommonResponse.of(this.policyService.createPolicy(odrlPolicyRequest, null));
    }

    @Tag(name = "Policy")
    @Operation(summary = "Policy evaluator for catalogue")
    @PostMapping(path = WizardRestConstant.POLICY_EVALUATE, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<JsonNode> evaluatePolicy(@Valid @RequestBody PolicyEvaluationRequest policyEvaluationRequest) {
        return CommonResponse.of(this.policyService.evaluatePolicy(policyEvaluationRequest));
    }


    @Operation(summary = "Policy evaluator for catalogue")
    @PostMapping(path = WizardRestConstant.POLICY_EVALUATE_V2, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<JsonNode> evaluatePolicyV2(@Valid @RequestBody PolicyEvaluationRequest policyEvaluationRequest) {
        return CommonResponse.of(this.policyService.evaluatePolicyV2(policyEvaluationRequest));
    }
}
