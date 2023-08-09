package eu.gaiax.wizard.api.model.service_offer;

import eu.gaiax.wizard.api.VerifiableCredential;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SignerServiceRequest {
    private String issuer;
    private String verificationMethod;
    private String privateKey;
    private VerifiableCredential vcs;
}
