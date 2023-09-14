package eu.gaiax.wizard.config.security.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("wizard.security")
public record SecurityConfigProperties(Boolean enabled,
                                       String clientId,
                                       String authUrl,
                                       String tokenUrl,
                                       String refreshTokenUrl,
                                       List<String> corsOrigins) {
}
