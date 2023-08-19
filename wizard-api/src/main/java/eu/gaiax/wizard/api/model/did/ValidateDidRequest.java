package eu.gaiax.wizard.api.model.did;

public record ValidateDidRequest(String did, String verificationMethod, String privateKey) {
}
