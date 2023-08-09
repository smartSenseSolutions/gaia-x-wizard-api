package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.core.service.service_offer.PolicyEvaluatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static eu.gaiax.wizard.api.model.StringPool.GAIA_X_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = GAIA_X_BASE_PATH)
public class PolicyEvaluatorController extends BaseResource {

    private final PolicyEvaluatorService policyEvaluatorService;

    @Tag(name = "Policy-Evaluator")
    @Operation(summary = "ODRL policy evaluator for catalogue")
    @PostMapping(path = "/policy/evaluate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<JsonNode> createServiceOfferingPublic(@Valid @RequestBody PolicyEvaluationRequest policyEvaluationRequest) throws IOException {
        return CommonResponse.of(this.policyEvaluatorService.evaluatePolicy(policyEvaluationRequest));
    }
}
