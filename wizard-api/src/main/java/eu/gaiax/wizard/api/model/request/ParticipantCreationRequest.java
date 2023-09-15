package eu.gaiax.wizard.api.model.request;

public record ParticipantCreationRequest(Boolean ownDid, String issuer, String verificationMethod,
                                         String privateKey, boolean store) {
}
