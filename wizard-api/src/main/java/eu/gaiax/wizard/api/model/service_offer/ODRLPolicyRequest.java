package eu.gaiax.wizard.api.model.service_offer;

import java.util.List;

public record ODRLPolicyRequest(List<String> rightOperand, String leftOperand, String target, String assigner, String domain, String serviceName, List<String> customAttribute) {
}
