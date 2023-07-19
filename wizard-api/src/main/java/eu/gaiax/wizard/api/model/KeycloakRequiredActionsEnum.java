package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum KeycloakRequiredActionsEnum {

    WEBAUTHN_REGISTER_PASSWORDLESS("webauthn-register-passwordless");

    private final String value;
}
