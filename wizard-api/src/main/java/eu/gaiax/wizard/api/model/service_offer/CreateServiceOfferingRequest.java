/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * The type
 * vice offering request.
 */
@Getter
@Setter
public class CreateServiceOfferingRequest {
    @NotNull(message = "service name required")
    private String name;
    private String email;
    private String description;
    private String privateKey;
    private String participantJsonUrl;
    private String verificationMethod;
    private boolean storeVault = false;
    @NotNull(message = "Credential subject required")
    private Map<String, Object> credentialSubject;
}
