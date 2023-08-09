package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;

public record PolicyEvaluationRequest(
        @NotNull(message = "Catalogue Description URL is required") String catalogueUrl,
        @NotNull(message = "Please enter service id") String serviceOfferId
) {
}
