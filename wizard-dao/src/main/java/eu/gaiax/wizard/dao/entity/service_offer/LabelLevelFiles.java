package eu.gaiax.wizard.dao.entity.service_offer;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "label_level_upload_files")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LabelLevelFiles extends SuperEntity {
    @Column(name = "participant_id", insertable = false, updatable = false)
    private UUID participantId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
    private Participant participant;
    @Column(name = "service_offer_id", insertable = false, updatable = false)
    private UUID serviceOfferId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_offer_id", referencedColumnName = "id", nullable = false)
    private ServiceOffer serviceOffer;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "description")
    private String description;
}
