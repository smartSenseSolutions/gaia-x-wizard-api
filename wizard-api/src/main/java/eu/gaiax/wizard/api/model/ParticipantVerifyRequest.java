package eu.gaiax.wizard.api.model;

import java.util.List;

public record ParticipantVerifyRequest(List<String> url, List<String> policies) {
}
