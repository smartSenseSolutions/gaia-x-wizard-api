package eu.gaiax.wizard.api.model.setting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Keycloak settings.
 */
@ConfigurationProperties(prefix = "keycloak")
@Configuration
@Getter
@Setter
public class KeycloakSettings {

    private String authServer;

    private String realm;

    private String clientId;

    private String clientSecret;

    private String webAuthRedirectUrl;

    private Integer actionTokenLifespan;
}

