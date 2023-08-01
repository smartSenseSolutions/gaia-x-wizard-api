package eu.gaiax.wizard.dao.entity.data_master;


import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "entity_type_master")
@Getter
@Setter
public class EntityTypeMaster extends SuperEntity implements BaseEntity {

    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private Boolean active;

}
