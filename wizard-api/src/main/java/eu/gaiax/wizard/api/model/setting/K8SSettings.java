/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type K 8 s settings.
 */
@ConfigurationProperties(prefix = "wizard.k8s")
public record K8SSettings(

    String basePath,

    String token,

    String serviceName
) {
}
