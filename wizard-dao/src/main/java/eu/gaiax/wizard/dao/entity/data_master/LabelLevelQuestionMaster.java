package eu.gaiax.wizard.dao.entity.data_master;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "label_level_question_master")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LabelLevelQuestionMaster extends SuperEntity {
    @Column(name = "type_id", insertable = false, updatable = false)
    private UUID typeId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false, referencedColumnName = "id")
    private LabelLevelTypeMaster type;
    @Column(name = "question", unique = true, nullable = false)
    private String question;
    @Column(name = "level_number", nullable = false)
    private String levelNumber;
    @Column(name = "active")
    private boolean active;
}
