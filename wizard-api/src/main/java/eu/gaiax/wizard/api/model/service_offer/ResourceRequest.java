package eu.gaiax.wizard.api.model.service_offer;

public record ResourceRequest(String resourceName, String description, String aggregationUrl,String type,String subType,boolean publish) {
}

