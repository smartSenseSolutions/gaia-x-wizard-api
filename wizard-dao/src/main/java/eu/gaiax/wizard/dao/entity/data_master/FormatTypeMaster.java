package eu.gaiax.wizard.dao.entity.data_master;


import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "format_type_master")
@Getter
@Setter
public class FormatTypeMaster extends SuperEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private Boolean active;

}
