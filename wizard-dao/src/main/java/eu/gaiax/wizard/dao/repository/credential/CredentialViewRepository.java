package eu.gaiax.wizard.dao.repository.credential;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.credential.CredentialView;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CredentialViewRepository extends BaseRepository<CredentialView, UUID> {
}
