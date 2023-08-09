package eu.gaiax.wizard.api.model.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Policy {
    @JsonProperty(value = "@context")
    private List<String> context;

    private String type;

    private String id;

    private List<Rule> permission;

    private List<Rule> prohibition;

    private List<Rule> duty;
}
