/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.ServiceOffer;

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
    private UUID id;
    private String description;
    private String privateKey;
    private String participantJson;
    private String verificationMethod;
    private boolean storeVault=false;
    private Map<String, Object> credentialSubject;
}
