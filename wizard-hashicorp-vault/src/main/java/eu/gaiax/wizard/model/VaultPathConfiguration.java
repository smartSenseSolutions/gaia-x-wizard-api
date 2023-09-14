package eu.gaiax.wizard.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wizard.vault")
public record VaultPathConfiguration(String secretPath) {
}
