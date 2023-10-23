package eu.gaiax.wizard.api.model.service_offer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class VerifiableCredential {

    private ServiceOffering serviceOffering;

    @Getter
    @Setter
    @Builder
    public static class ServiceOffering {
        @JsonProperty("@context")
        private List<String> context;

        private String type;

        private String id;

        private String issuer;

        private String issuanceDate;

        private Map<String, Object> credentialSubject;
    }
}
