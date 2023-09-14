package eu.gaiax.wizard.api.model;

import java.util.UUID;


public interface LabelLevelQuestionInterface {

    UUID getId();

    String getCriterionNumber();

    String getQuestion();

    String getHighestLabelLevel();

}
