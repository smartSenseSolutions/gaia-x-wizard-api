/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import java.util.Map;

/**
 * The type Create participant request.
 */
public record CreateVCRequest(String privateKey, String issuer, String verificationMethod, Map<String, Object> vcs) {
}
