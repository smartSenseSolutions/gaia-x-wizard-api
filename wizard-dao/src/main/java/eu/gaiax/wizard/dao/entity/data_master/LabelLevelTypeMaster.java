package eu.gaiax.wizard.dao.entity.data_master;

import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "label_level_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelLevelTypeMaster extends SuperEntity implements BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "type", fetch = FetchType.EAGER)
    private List<LabelLevelQuestionMaster> labelLevelQuestionMasterList;

}
