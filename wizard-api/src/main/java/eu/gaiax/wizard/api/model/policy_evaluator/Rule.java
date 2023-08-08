package eu.gaiax.wizard.api.model.policy_evaluator;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Rule {
    private String target;

    private String assigner;

    private String assignee;

    private String action;

    private List<Constraint> constraint;

}
