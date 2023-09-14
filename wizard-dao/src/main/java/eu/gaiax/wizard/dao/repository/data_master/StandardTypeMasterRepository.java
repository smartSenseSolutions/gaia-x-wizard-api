/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.data_master.StandardTypeMaster;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * The interface Standard type master repository.
 */
@Repository
public interface StandardTypeMasterRepository extends BaseRepository<StandardTypeMaster, String> {

    List<StandardTypeMaster> findAllByTypeIn(List<String> standardNameList);
}
