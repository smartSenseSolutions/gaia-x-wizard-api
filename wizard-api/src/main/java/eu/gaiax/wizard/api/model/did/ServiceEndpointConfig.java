package eu.gaiax.wizard.api.model.did;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wizard.service-endpoint")
public record ServiceEndpointConfig(String linkDomainType, String pdpType, String pdpUrl) {
}
