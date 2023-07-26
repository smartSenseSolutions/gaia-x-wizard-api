package eu.gaiax.wizard.dao.entity.serviceoffer;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.data_master.LabelLevelQuestionMaster;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "label_level_answer")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LabelLevelAnswer extends SuperEntity {
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID participantId;
    @ManyToOne
    @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
    private Participant participant;
    @Column(name = "service_offer_id", nullable = false, updatable = false)
    private UUID serviceOfferId;
    @ManyToOne
    @JoinColumn(name = "service_offer_id", referencedColumnName = "id", nullable = false)
    private ServiceOffer serviceOffer;
    @Column(name = "question_id", nullable = false, updatable = false)
    private UUID questionId;
    @ManyToOne
    @JoinColumn(name = "question_id", referencedColumnName = "id", nullable = false)
    private LabelLevelQuestionMaster question;
    private boolean answer;
}
