/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.api.model.policy.SubdivisionName;
import eu.gaiax.wizard.dao.entity.data_master.SubdivisionCodeMaster;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * The interface Standard type master repository.
 */
@Repository
public interface SubdivisionCodeMasterRepository extends BaseRepository<SubdivisionCodeMaster, String> {

    List<SubdivisionName> findAllNameBySubdivisionCodeIn(String[] subdivisionCodeArray);
}
