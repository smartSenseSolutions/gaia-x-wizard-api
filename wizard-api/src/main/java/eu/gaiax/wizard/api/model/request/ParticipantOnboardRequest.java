package eu.gaiax.wizard.api.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record ParticipantOnboardRequest(
        @NotNull(message = "invalid.legal.name") @Size(min = 1, message = "invalid.legal.name") String legalName,
        @Pattern(regexp = "[0-9a-zA-Z]$", message = "invalid.short.name") String shortName,
        String entityType,
        //TODO credential contain legalName, shortName, MultipleRegistrationType & values,
        //TODO entity Type id, parent and sub organizations, headquarter and legal address
        Map<String, Object> credential, boolean ownDid, boolean store,
        boolean acceptedTnC) {
}
