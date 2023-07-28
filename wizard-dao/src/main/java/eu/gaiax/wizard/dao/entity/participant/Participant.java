package eu.gaiax.wizard.dao.entity.participant;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participant")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Participant extends SuperEntity {
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "did", nullable = false, unique = true)
    private String did;
    @Column(name = "legal_name")
    private String legalName;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "entity_type_id", insertable = false, updatable = false)
    private String entityTypeId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_type_id", referencedColumnName = "id")
    private EntityTypeMaster entityType;
    @Column(name = "sub_domain")
    private String subDomain;
    @Column(name = "private_key_id")
    private String privateKeyId;
    @Column(name = "participant_type")
    private String participantType;

}
