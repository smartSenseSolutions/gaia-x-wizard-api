package eu.gaiax.wizard.api.model.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "wizard.context")
public record ContextConfig(List<String> serviceOffer,
                            List<String> participant,
                            List<String> registrationNumber,
                            List<String> tnc, List<String> ODRLPolicy) {
}
