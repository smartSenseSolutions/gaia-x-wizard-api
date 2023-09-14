package eu.gaiax.wizard.core.service.participant.model.request;

public record ParticipantValidatorRequest(String participantJsonUrl, String verificationMethod, String privateKey,
                                          boolean store, boolean ownDid) {
}
