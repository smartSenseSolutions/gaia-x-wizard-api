package eu.gaiax.wizard.api.model.service_offer;

import java.util.Map;

public record CreateResourceRequest(Map<String, Object> credentialSubject, boolean publish, String email,
                                    String privateKey, String participantJson, String verificationMethod,
                                    boolean vault) {
}

