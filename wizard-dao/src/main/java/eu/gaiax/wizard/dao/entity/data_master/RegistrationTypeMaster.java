package eu.gaiax.wizard.dao.entity.data_master;


import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "registration_type_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationTypeMaster extends SuperEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private Boolean active;

}
