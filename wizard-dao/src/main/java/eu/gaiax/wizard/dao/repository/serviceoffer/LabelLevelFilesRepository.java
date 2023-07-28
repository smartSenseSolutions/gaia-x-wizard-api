package eu.gaiax.wizard.dao.repository.serviceoffer;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.serviceoffer.LabelLevelFiles;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LabelLevelFilesRepository extends BaseRepository<LabelLevelFiles, UUID> {
}
