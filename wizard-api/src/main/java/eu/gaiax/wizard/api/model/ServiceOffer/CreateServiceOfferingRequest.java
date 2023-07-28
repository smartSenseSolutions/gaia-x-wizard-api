/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.ServiceOffer;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * The type Create service offering request.
 */
@Getter
@Setter
public class CreateServiceOfferingRequest {

    private String name;

    private UUID id;

    private String description;

    private String privateKey;

/*    private String policy;

    private String accessType;

    private String requestType;

    private String formatType;

    private String terms;*/

    private Map<String, Object> credentialSubject;
}
