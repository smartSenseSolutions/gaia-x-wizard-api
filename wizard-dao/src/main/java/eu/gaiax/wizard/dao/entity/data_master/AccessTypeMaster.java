package eu.gaiax.wizard.dao.entity.data_master;


import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

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

    @Column(name = "active")
    private Boolean active;
}
