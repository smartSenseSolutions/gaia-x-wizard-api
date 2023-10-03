package eu.gaiax.wizard.api.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

public record ParticipantRegisterRequest(@Email(message = "email.required") String email,
                                         @Valid ParticipantOnboardRequest onboardRequest) {
}
