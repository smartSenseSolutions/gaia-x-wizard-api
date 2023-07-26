package eu.gaiax.wizard.dao.entity.serviceoffer;

import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "service_offer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServiceOffer extends SuperEntity {

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "credential_id", nullable = false, updatable = false)
    private UUID credentialId;
    @JoinColumn(name = "credential_id", nullable = false, referencedColumnName = "id")
    private Credential credential;
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID participantId;
    @OneToOne
    @JoinColumn(name = "participant_id", nullable = false, referencedColumnName = "id")
    private Participant participant;
}
