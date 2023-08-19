package eu.gaiax.wizard.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SendRegistrationEmailRequest(
        @Email(message = "Please enter valid email") @NotNull(message = "Please enter valid email") String email
) {
}
