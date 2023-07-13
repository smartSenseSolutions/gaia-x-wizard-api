/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * The type Create service offering request.
 */
@Getter
@Setter
public class CreateServiceOfferingRequest {

    private String name;

    private String description;

    private String policy;

    private String accessType;

    private String requestType;

    private String formatType;

    private String terms;


    private Map<String, Object> meta;
}
