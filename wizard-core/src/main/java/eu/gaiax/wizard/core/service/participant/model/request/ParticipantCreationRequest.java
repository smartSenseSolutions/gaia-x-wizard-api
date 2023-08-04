package eu.gaiax.wizard.core.service.participant.model.request;

public record ParticipantCreationRequest(String issuer, String verificationMethod, String privateKey, boolean store) {
}
