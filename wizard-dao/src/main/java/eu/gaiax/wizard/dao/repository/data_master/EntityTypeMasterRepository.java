/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * The interface Entity type master repository.
 */
@Repository
public interface EntityTypeMasterRepository extends BaseRepository<EntityTypeMaster, UUID> {

}
