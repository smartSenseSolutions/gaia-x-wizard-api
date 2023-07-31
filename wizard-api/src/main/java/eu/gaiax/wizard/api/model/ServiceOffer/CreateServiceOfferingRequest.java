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
    private String verificationMethod;
    private Map<String, Object> credentialSubject;
}
