/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models.setting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The type Jwt setting.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JWTSetting {

    private String tokenSigningKey;

}
