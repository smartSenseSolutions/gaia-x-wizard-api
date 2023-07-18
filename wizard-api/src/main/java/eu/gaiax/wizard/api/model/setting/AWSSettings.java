/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model.setting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Aws settings.
 *
 * @author Nitin
 * @version 1.0
 */
@ConfigurationProperties(prefix = "aws")
@Configuration
@Getter
@Setter
public class AWSSettings {

    private String accessKey;

    private String secretKey;

    private String hostedZoneId;

    private String serverIp;

    private String baseDomain;
}
