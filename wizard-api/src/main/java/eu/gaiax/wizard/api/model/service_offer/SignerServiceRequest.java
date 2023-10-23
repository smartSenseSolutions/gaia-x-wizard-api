package eu.gaiax.wizard.api.model.service_offer;


public record SignerServiceRequest(
        String issuer,
        String verificationMethod,
        String privateKey,
        VerifiableCredential vcs,
        boolean isVault) {

}
