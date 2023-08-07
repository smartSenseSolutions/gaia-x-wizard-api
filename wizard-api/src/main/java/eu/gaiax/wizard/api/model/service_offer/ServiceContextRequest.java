package eu.gaiax.wizard.api.model.service_offer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ServiceContextRequest {
    private Map<String,Object> context;
}
