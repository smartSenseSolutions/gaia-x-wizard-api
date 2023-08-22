package eu.gaiax.wizard.config.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wizard.info")
public record ApplicationInfo(String name, String description, String version, ContactInfo contact) {
}
