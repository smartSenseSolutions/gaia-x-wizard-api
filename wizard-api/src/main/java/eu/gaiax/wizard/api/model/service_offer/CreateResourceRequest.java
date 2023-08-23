package eu.gaiax.wizard.api.model.service_offer;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateResourceRequest {
    private Map<String, Object> credentialSubject;
    private boolean publish;
    private String email;
    private String privateKey;
    private String participantJson;
    private String verificationMethod;
    private boolean storeVault;
    private boolean ownCertificate;
}

