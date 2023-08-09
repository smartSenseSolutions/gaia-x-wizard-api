package eu.gaiax.wizard.api.model;

import java.util.List;

public record ParticipantVerifyRequest(String url, List<String> policies) {
}
