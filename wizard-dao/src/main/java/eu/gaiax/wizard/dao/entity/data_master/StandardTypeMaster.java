package eu.gaiax.wizard.dao.entity.data_master;


import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.gaiax.wizard.dao.entity.SuperEntity;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "standard_type_master")
@Getter
@Setter
public class StandardTypeMaster extends SuperEntity {

    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private Boolean active;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "serviceOfferStandardType")
    private List<ServiceOffer> serviceOfferList;
}
