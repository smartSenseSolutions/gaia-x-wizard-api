/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * The type
 * vice offering request.
 */
@Getter
@Setter
public class CreateServiceOfferingRequest {
    @NotNull(message = "service name required")
    @Size(max = 255, message = "Name exceeds maximum character limit")
    private String name;
    private String email;
    private UUID id;
    private String issuer;
    @Size(max = 500, message = "Description exceeds maximum character limit")
    private String description;
    private String privateKey;
    private String participantJsonUrl;
    private String verificationMethod;
    private boolean storeVault = false;
    @NotNull(message = "Credential subject required")
    private Map<String, Object> credentialSubject;
}
