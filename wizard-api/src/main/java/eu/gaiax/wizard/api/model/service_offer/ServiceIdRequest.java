package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;

public record ServiceIdRequest(@NotNull(message = "Please enter service Id") String id) {
}
