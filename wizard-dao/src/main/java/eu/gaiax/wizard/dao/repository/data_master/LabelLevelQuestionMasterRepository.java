/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.api.model.ApplicableLevelCriterionEnum;
import eu.gaiax.wizard.dao.entity.data_master.LabelLevelQuestionMaster;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Format type master repository.
 */
@Repository
public interface LabelLevelQuestionMasterRepository extends BaseRepository<LabelLevelQuestionMaster, String> {


    List<String> findAllCriterionNumberByLevel1In(List<ApplicableLevelCriterionEnum> applicableLevelCriterionEnumList);

}
