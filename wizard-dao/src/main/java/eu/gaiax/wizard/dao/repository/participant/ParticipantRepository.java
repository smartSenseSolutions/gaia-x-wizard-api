package eu.gaiax.wizard.dao.repository.participant;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParticipantRepository extends BaseRepository<Participant, UUID> {
    Participant getByEmail(String email);

    Participant getByDid(String did);
}
