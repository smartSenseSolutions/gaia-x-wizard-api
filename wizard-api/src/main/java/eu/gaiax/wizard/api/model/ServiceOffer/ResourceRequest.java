package eu.gaiax.wizard.api.model.ServiceOffer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ResourceRequest(String resourceName, String description, String aggregationUrl,String type,String subType,boolean publish) {
}

