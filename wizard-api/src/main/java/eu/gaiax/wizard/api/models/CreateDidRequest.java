/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Create did request.
 */
@Getter
@Setter
@Builder
public class CreateDidRequest {

    private String domain;
}
