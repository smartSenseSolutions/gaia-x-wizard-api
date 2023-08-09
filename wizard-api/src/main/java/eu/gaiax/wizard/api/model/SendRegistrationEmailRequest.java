package eu.gaiax.wizard.api.model;

import jakarta.validation.constraints.NotNull;

public record SendRegistrationEmailRequest(@NotNull(message = "Please enter valid email") String email) {
}