package eu.gaiax.wizard.api.model.policy_evaluator;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Policy {
    private String id;

    private List<String> context;

    private String type;

    private String uid;

    private List<Rule> permission;

    private List<Rule> prohibition;

    private List<Rule> duty;
}
