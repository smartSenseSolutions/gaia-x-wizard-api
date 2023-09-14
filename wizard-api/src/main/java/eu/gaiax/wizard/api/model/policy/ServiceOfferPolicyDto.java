package eu.gaiax.wizard.api.model.policy;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public record ServiceOfferPolicyDto(
        @JsonAlias("gx:location") List<String> location,
        @JsonAlias("gx:customAttribute") String customAttribute) {
}
