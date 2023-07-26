package eu.gaiax.wizard.dao.entity.data_master;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "label_level_type_master")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LabelLevelTypeMaster extends SuperEntity {
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "name", nullable = false)
    private String name;
}
