package eu.gaiax.wizard.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import eu.gaiax.wizard.api.model.service_offer.CredentialDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class ResourceFilterResponse {

    private UUID id;

    private String name;

    private String typeLabel;

    private String type;

    @JsonAlias("vcUrl")
    private String selfDescription;

    private Date createdAt;

    private CredentialDto credential;
}
