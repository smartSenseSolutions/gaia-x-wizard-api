package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ParticipantAndKeyResponse {
    private String participantJson;

    private String privateKey;
}
