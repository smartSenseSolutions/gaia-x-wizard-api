package eu.gaiax.wizard.api.model.setting;

import org.springframework.boot.context.properties.*;

/**
 * The type Keycloak settings.
 */
@ConfigurationProperties(prefix = "wizard.keycloak")
public record KeycloakSettings(

        String authServer,

        String realm,

        String clientId,

        String clientSecret,

        Integer actionTokenLifespan,

        String requiredActionsEmailRedirectionUrl,
        String publicClientId
) {
}

