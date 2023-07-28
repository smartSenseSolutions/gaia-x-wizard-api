/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import java.util.Map;

/**
 * The type Create participant request.
 */
public record CreateVCRequest(String domain, String templateId, String did, Map<String, Object> credentials) {
}
