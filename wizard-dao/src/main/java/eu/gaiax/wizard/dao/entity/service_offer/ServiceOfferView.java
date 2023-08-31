package eu.gaiax.wizard.dao.entity.service_offer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsensesolutions.java.commons.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.util.Date;
import java.util.UUID;

@Entity
@Immutable
@Getter
@Setter
@Subselect(value = "select so.id as id, so.name as name, so.participant_id, so.created_at as created_at, c.vc_url as self_description, string_agg(stm.type, ', ') as supported_type_list from service_offer so inner join credential c on so.credential_id = c.id inner join service_offer_standard_type sost on so.id = sost.service_offer_id inner join standard_type_master stm on stm.id = sost.standard_type_id group by so.id, so.name, so.participant_id, so.created_at, c.vc_url")
public class ServiceOfferView implements BaseEntity {

    @Id
    private UUID id;
    private String name;
    @JsonIgnore
    private UUID participantId;
    private String selfDescription;
    private Date createdAt;
    private String supportedTypeList;
}
