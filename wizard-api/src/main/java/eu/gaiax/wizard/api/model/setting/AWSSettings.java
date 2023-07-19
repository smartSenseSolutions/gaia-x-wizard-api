/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Aws settings.
 *
 * @author Nitin
 * @version 1.0
 */
@ConfigurationProperties(prefix = "aws")
public record AWSSettings(String bucket,
                          String region,
                          String accessKey,
                          String secretKey,
                          String hostedZoneId,
                          String serverIp,
                          String baseDomain) {

}
