package eu.gaiax.wizard.dao.entity.service_offer;

import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "service_label_level")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServiceLabelLevel extends SuperEntity {


    @Column(name = "credential_id", insertable = false, updatable = false)
    private UUID credentialId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // Adjust the cascade type as per your use case
    @JoinColumn(name = "credential_id", nullable = false, referencedColumnName = "id")
    private Credential credential;

    @Column(name = "participant_id", insertable = false, updatable = false)
    private UUID participantId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // Adjust the cascade type as per your use case
    @JoinColumn(name = "participant_id", nullable = false, referencedColumnName = "id")
    private Participant participant;


    @Column(name = "service_offer_id", insertable = false, updatable = false)
    private UUID serviceOfferId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // Adjust the cascade type as per your use case
    @JoinColumn(name = "service_offer_id", nullable = false, referencedColumnName = "id")
    private ServiceOffer serviceOffer;

    public String getVcUrl() {
        if (this.credential != null) {
            return this.credential.getVcUrl();
        }
        return null;
    }
}


