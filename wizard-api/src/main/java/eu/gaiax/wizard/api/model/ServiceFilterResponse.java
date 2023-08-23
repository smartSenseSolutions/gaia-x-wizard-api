package eu.gaiax.wizard.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    private Integer labelLevel;

    @JsonAlias("vcUrl")
    private String selfDescription;

    private List<StandardTypeDto> serviceOfferStandardType;

    private Date createdAt;

    @Getter
    @Setter
    public static class StandardTypeDto {

        private UUID id;
        private String type;
    }

}
