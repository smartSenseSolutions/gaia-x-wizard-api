package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record LabelLevelRequest(
        @NotNull(message = "credentialSubject is required")
        Map<String, Object> criteria, String privateKey, String participantJson, String verificationMethod,
        boolean vault
) {
}


