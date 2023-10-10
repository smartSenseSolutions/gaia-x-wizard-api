package eu.gaiax.wizard.dao.repository;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.Credential;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CredentialRepository extends BaseRepository<Credential, UUID> {

    Credential findByParticipantIdAndCredentialType(UUID participantId, String credentialType);
}
