package eu.gaiax.wizard.api.model.policy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Constraint {
    private String leftOperand;

    private String name;

    private String operator;

    private String[] rightOperand;
}
