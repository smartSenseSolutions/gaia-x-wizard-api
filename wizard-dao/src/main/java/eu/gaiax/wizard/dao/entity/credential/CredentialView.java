package eu.gaiax.wizard.dao.entity.credential;

import eu.gaiax.wizard.dao.entity.SuperEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "credential_view")
@Getter
@Setter
public class CredentialView extends SuperEntity {

    @Column(name = "vc_url", nullable = false)
    private String vcUrl;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "participant_id", insertable = false, updatable = false)
    private UUID participantId;

    @Column(name = "name")
    private String name;
}
