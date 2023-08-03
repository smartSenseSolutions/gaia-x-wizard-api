package eu.gaiax.wizard.api.model;

import java.util.List;

public record ParticipantVerifyRequest(String participantUrl, List<String> policies) {
}
