package eu.gaiax.wizard.dao.entity.resource;

import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "resource")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Resource extends SuperEntity implements BaseEntity {
    @Column(name = "credential_id", insertable = false, updatable = false)
    private UUID credentialId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", nullable = false, referencedColumnName = "id")
    private Credential credential;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type")
    private String subType;

    @Column(name = "participant_id", insertable = false, updatable = false)
    private UUID participantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false, referencedColumnName = "id")
    private Participant participant;
    @Column(name = "publish_to_kafka", nullable = false)

    private boolean publishToKafka;

    public String getVcUrl() {
        if (this.credential != null) {
            return this.credential.getVcUrl();
        }
        return null;
    }
}
