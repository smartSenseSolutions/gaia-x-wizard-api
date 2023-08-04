package eu.gaiax.wizard.api.model.ServiceOffer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ServiceOfferResponse {
    private String name;
    private String description;
    private  String vcUrl;
    private Map<Object,Object> vcJson;
    private String veracityData ;
}
