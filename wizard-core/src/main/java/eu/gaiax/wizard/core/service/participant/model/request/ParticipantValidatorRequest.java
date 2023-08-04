package eu.gaiax.wizard.core.service.participant.model.request;

public record ParticipantValidatorRequest(String participantJsonUrl, String verificationMethod, String privateKey,
                                          String issuer, boolean store) {
}
