package eu.gaiax.wizard.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParticipantProfileDto {

    private String id;

    private String legalName;

    private String shortName;

    private String headquarterAddress;

    private String legalAddress;

    private JsonNode legalRegistrationNumber;

    private String profileImage;

    private List<String> parentOrganization;

    private List<String> subOrganization;
}
