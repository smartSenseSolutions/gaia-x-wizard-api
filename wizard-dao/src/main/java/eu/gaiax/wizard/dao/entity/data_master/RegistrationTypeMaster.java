package eu.gaiax.wizard.dao.entity.data_master;


import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "registration_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationTypeMaster extends SuperEntity implements BaseEntity {

    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private Boolean active;

}
