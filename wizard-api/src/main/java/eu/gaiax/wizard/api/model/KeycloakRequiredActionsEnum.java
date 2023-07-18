package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum KeycloakRequiredActionsEnum {

    WEB_AUTHN("webauthn-register");

    private final String value;
}
