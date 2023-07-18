/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * The type Create participant request.
 */
@Getter
@Setter
@Builder
public class CreateVCRequest {

    private String domain;

    private String templateId;

    private String privateKeyUrl;

    private Map<String, String> data;

}
