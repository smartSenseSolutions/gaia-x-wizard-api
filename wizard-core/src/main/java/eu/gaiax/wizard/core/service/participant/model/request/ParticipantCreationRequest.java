package eu.gaiax.wizard.core.service.participant.model.request;

public record ParticipantCreationRequest(Boolean ownDid, String issuer, String verificationMethod,
                                         String privateKey, boolean store) {
}
