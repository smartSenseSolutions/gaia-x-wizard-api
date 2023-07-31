package eu.gaiax.wizard.api.model.ServiceOffer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResourceRequest {

    @Email
    @NotBlank
    private String resourceName;
    private String description;
    private String aggregationUrl;

}
