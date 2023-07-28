package eu.gaiax.wizard.dao.entity.data_master;


import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subdivision_code_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubdivisionCodeMaster extends SuperEntity implements BaseEntity {

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "subdivision_code")
    private String subdivisionCode;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private Boolean active;
}
