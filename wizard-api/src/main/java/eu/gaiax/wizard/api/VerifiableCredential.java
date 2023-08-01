package eu.gaiax.wizard.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;
@Getter
@Setter
@Builder
public class VerifiableCredential {
    @JsonProperty("@context")
    private List<String> context;

    private String templateId;

    private String privateKeyID;

    private String legalParticipate;

    private String type;

    private String id;

    private String issuer;

    private String issuanceDate;

    private Map<String,Object> credentialSubject;
}
