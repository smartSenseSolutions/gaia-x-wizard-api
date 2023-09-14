package eu.gaiax.wizard.api.model.service_offer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class ServiceOfferingLocationResponse {
    private List<String> serviceAvailabilityLocation;
}
