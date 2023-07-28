package eu.gaiax.wizard.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CredentialTypeEnum {

    SERVICE_OFFER("service_offer"),

    LEGAL_PARTICIPANT("legal_participant"),

    REGISTRATION_NUMBER("registration_number"),

    RESOURCE("resource"),

    TERM_CONDITION("term_condition");

    private String credentialType;
}
