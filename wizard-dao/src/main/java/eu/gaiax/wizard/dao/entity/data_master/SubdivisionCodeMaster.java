package eu.gaiax.wizard.dao.entity.data_master;


import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subdivision_code_master")
@Getter
@Setter
public class SubdivisionCodeMaster extends SuperEntity {

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "subdivision_code")
    private String subdivisionCode;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private Boolean active;
}
