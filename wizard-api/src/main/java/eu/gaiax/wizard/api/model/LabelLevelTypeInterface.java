package eu.gaiax.wizard.api.model;

import java.util.List;
import java.util.UUID;


public interface LabelLevelTypeInterface {

    UUID getId();

    String getName();

    List<LabelLevelQuestionInterface> getLabelLevelQuestionMasterList();

}
