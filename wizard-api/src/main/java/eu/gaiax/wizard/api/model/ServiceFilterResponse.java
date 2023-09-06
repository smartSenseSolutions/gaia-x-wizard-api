package eu.gaiax.wizard.api.model;

import eu.gaiax.wizard.api.model.service_offer.CredentialDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ServiceFilterResponse {

    private UUID id;

    private String name;

    private String labelLevel;

    private CredentialDto credential;

    private List<StandardTypeDto> serviceOfferStandardType;

    private Date createdAt;
}
