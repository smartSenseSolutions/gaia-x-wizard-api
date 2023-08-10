/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.did;


import java.util.List;

public record CreateDidRequest(String domain, List<ServiceEndpoints> services) {
}
