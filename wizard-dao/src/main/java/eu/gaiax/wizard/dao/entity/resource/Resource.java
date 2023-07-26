package eu.gaiax.wizard.dao.entity.resource;

import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "resource")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Resource extends SuperEntity {
    @OneToOne
    @JoinColumn(name = "credential_id", referencedColumnName = "id")
    private Credential credential;
    @Column(name = "credential_id", nullable = false, updatable = false)
    private UUID credentialId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "type")
    private String type;
    @Column(name = "sub_type")
    private String subType;
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID participantId;
    @OneToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;
    @Column(name = "publish_to_kafka", nullable = false)
    private boolean publishToKafka;
}
