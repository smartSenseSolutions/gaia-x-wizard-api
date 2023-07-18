/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Login response.
 */
@Getter
@Setter
@Builder
public class LoginResponse {

    private String token;

    private SessionDTO session;
}
