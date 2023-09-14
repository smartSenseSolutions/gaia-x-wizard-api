package eu.gaiax.wizard.api.model.service_offer;

import eu.gaiax.wizard.api.VerifiableCredential;


public record SignerServiceRequest(
        String issuer,
        String verificationMethod,
        String privateKey,
        VerifiableCredential vcs,
        boolean isVault) {

}
