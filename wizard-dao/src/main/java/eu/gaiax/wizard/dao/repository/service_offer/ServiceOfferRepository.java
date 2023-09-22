package eu.gaiax.wizard.dao.repository.service_offer;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceOfferRepository extends BaseRepository<ServiceOffer, UUID> {

    @Transactional
    @Modifying
    @Query("UPDATE ServiceOffer s SET s.messageReferenceId = :messageReferenceId WHERE s.id = :id")
    void updateMessageReferenceId(UUID id, String messageReferenceId);
}
