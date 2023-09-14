package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ParticipantConfigDTO {

    private UUID id;
    private String email;
    private String did;
    private String legalName;
    private String participantType;
    private boolean ownDidSolution;
    private boolean keyStored;
    private Boolean privateKeyRequired;
    private String legalParticipantUrl;
    private int status;

}
