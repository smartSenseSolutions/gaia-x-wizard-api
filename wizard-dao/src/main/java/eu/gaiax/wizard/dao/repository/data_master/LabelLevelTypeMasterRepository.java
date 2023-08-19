/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.api.model.LabelLevelTypeInterface;
import eu.gaiax.wizard.dao.entity.data_master.LabelLevelTypeMaster;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Format type master repository.
 */
@Repository
public interface LabelLevelTypeMasterRepository extends BaseRepository<LabelLevelTypeMaster, String> {

    List<LabelLevelTypeInterface> findAllByActiveIsTrueAndLabelLevelQuestionMasterListIsNotNull();
}
