package eu.gaiax.wizard.api.model.ServiceOffer;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.gaiax.wizard.api.VerifiableCredential;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class SignerServiceRequest {

    private String templateId;

    private String privateKeyID;

    private String legalParticipate;

    private List<VerifiableCredential> verifiableCredential;
}
