package eu.gaiax.wizard.dao.entity;

import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "credential")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Credential extends SuperEntity {
    @Column(name = "vc_url", nullable = false)
    private String vcUrl;
    @Column(name = "vc_json", nullable = false)
    private String vcJson;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID participantId;
    @Column(name = "metadata")
    private String metadata;
    @ManyToOne
    @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
    private Participant participant;
}

