package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantConfigDTO {

    private String email;
    private String did;
    private String legalName;
    private String participantType;
    private boolean ownDidSolution;
    private String legalParticipantUrl;

}
