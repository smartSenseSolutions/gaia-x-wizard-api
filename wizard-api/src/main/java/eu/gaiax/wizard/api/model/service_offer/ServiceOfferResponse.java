package eu.gaiax.wizard.api.model.service_offer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ServiceOfferResponse {
    private String name;
    private String description;
    private  String vcUrl;
    private List<Map<Object,Object>> vcJson;
    private String veracityData ;
}
