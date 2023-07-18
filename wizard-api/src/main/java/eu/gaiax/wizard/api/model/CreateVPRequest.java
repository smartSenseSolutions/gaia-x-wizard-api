/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * The type Create vp request.
 */
@Getter
@Setter
@Builder
public class CreateVPRequest {

    private String privateKeyUrl;

    private String holderDID;

    private List<Map<String, Object>> claims;
}
