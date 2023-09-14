package eu.gaiax.wizard.dao.repository.service_offer;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.service_offer.LabelLevelFiles;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LabelLevelFilesRepository extends BaseRepository<LabelLevelFiles, UUID> {
}
