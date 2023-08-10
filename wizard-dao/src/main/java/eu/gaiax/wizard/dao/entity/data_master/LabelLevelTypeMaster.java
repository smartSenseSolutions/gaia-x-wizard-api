package eu.gaiax.wizard.dao.entity.data_master;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "label_level_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelLevelTypeMaster extends SuperEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "type", fetch = FetchType.EAGER)
    private List<LabelLevelQuestionMaster> labelLevelQuestionMasterList;

    public LabelLevelTypeMaster(UUID id, String name, List<LabelLevelQuestionMaster> labelLevelQuestionMasterList) {
        super(id);
        this.name = name;
        this.labelLevelQuestionMasterList = labelLevelQuestionMasterList;
    }
}
