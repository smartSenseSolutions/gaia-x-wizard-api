/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.service_offer;

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
    private String name;
    private String email;
    private UUID id;
    private String issuer;
    private String description;
    private String privateKey;
    private String participantJsonUrl;
    private String verificationMethod;
    private boolean storeVault=false;
    private Map<String, Object> credentialSubject;
}
