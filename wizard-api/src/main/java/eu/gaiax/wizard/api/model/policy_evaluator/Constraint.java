package eu.gaiax.wizard.api.model.policy_evaluator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Constraint {
    private String leftOperand;

    private String operator;

    private String[] rightOperand;
}
