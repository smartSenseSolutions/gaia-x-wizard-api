package eu.gaiax.wizard.api.model.setting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "context")
public class ContextConfig {
    List<String> serviceOffer;
 }
