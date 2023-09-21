package eu.gaiax.wizard.api.model.request;

public record ParticipantValidatorRequest(String participantJsonUrl, String verificationMethod, String privateKey,
                                          boolean store, boolean ownDid) {
}
