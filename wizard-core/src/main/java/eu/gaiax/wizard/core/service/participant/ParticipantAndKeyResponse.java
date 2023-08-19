package eu.gaiax.wizard.core.service.participant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ParticipantAndKeyResponse {
    private String participantJson;

    private String privateKey;
}
