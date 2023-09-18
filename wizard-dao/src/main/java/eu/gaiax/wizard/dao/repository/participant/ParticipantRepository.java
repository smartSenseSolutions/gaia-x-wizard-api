package eu.gaiax.wizard.dao.repository.participant;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParticipantRepository extends BaseRepository<Participant, UUID> {
    Participant getByEmail(String email);

    Participant getByDomain(String domain);

    Participant getByDid(String did);

    Participant getByLegalName(String legalName);

    Participant getByShortName(String shortName);

    boolean existsByEmail(String email);
}
