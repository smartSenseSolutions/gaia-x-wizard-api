package eu.gaiax.wizard.dao.repository.resource;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceRepository extends BaseRepository<Resource, UUID> {
}
