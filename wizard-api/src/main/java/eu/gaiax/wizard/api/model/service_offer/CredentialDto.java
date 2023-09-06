package eu.gaiax.wizard.api.model.service_offer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CredentialDto {

    private UUID id;
    private String vcUrl;
}
