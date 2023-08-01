package eu.gaiax.wizard.dao.entity.data_master;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.api.model.ApplicableLevelCriterionEnum;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "label_level_question_master")
@Getter
@Setter
public class LabelLevelQuestionMaster extends SuperEntity implements BaseEntity {

    @Column(name = "type_id", insertable = false, updatable = false)
    private UUID typeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private LabelLevelTypeMaster type;

    @Column(name = "criterion_number")
    private String criterionNumber;

    @Column(name = "question")
    private String question;

    @Column(name = "basic_conformity")
    @Enumerated(EnumType.STRING)
    private ApplicableLevelCriterionEnum basicConformity;

    @Column(name = "level_1")
    @Enumerated(EnumType.STRING)
    private ApplicableLevelCriterionEnum level1;

    @Column(name = "active")
    private boolean active;

    public LabelLevelQuestionMaster(UUID id, String criterionNumber, String question) {
        super(id);
        this.criterionNumber = criterionNumber;
        this.question = question;
    }
}
