package eu.gaiax.wizard.dao.entity.data_master;


import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "access_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessTypeMaster extends SuperEntity implements BaseEntity {


    @Column(name = "type")
    private String type;

    @Column(name = "is_active")
    private Boolean active;
}
