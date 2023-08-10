package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ServiceAndResourceListDTO {

    private UUID id;

    private String name;

    private String vcUrl;
}
