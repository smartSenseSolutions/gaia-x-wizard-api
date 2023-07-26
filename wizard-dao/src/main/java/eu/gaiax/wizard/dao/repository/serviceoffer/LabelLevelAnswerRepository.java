package eu.gaiax.wizard.dao.repository.serviceoffer;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.serviceoffer.LabelLevelAnswer;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LabelLevelAnswerRepository extends BaseRepository<LabelLevelAnswer, UUID> {
}
