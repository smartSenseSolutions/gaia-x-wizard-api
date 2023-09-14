package eu.gaiax.wizard.dao.entity.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.gaiax.wizard.api.model.ResourceType;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.credential.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "resource")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Resource extends SuperEntity {

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

    @Column(name = "participant_id", insertable = false, updatable = false)
    private UUID participantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false, referencedColumnName = "id")
    private Participant participant;

    @Column(name = "publish_to_kafka", nullable = false)
    private boolean publishToKafka;

    @Column(name = "obsolete_date", nullable = false)
    private Date obsoleteDate;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    public String getVcUrl() {
        if (this.credential != null) {
            return this.credential.getVcUrl();
        }
        return null;
    }

    @Override
    @JsonIgnore(value = false)
    @JsonProperty
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    public String getTypeLabel() {
        return ResourceType.getByValue(this.type) != null ? ResourceType.getByValue(this.type).getLabel() : null;
    }
}
