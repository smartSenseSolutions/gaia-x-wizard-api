package eu.gaiax.wizard.dao.entity.data_master;

import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "label_level_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelLevelTypeMaster extends SuperEntity implements BaseEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "type", fetch = FetchType.EAGER)
    private List<LabelLevelQuestionMaster> labelLevelQuestionMasterList;

    public LabelLevelTypeMaster(UUID id, String name, List<LabelLevelQuestionMaster> labelLevelQuestionMasterList) {
        this.id = id;
        this.name = name;
        this.labelLevelQuestionMasterList = labelLevelQuestionMasterList;
    }
}
