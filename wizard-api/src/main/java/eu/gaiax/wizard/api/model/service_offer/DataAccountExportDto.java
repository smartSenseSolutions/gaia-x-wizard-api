package eu.gaiax.wizard.api.model.service_offer;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataAccountExportDto {

    private String accessType;

    private String requestType;

    private Set<String> formatType;
}
