package eu.gaiax.wizard.api.model.service_offer;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ServiceDetailResponse {

    private UUID id;

    private CredentialDto credential;

    private String name;

    private String description;

    private String labelLevel;

    private Set<String> protectionRegime = new HashSet<>();

    private Set<String> locations = new HashSet<>();

    private Set<AggregateAndDependantDto> dependedServices = new HashSet<>();

    private Set<AggregateAndDependantDto> resources = new HashSet<>();

    private Double trustIndex;

    private DataAccountExportDto dataAccountExport;

    private String tnCUrl;

    private ServiceProviderDto participant;

    private Double trustScore;
}
