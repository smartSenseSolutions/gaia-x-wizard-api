/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.util.Map;

/**
 * The type Service offer view.
 */
@Entity
@Subselect(value = "select so.id , so.credential_id , so.subject_did , so .\"name\" ,so.label, so.produced_by , so.copyright_owned_by , so.description , so.terms, so.terms_hash , so.access_type , so.request_type , so.format_type  , e.id as enterprise_id, e.legal_name as enterprise_name, e.sub_domain_name  from service_offer so inner join enterprise e  on e.id = so.enterprise_id")
@Immutable
@Getter
@Setter
public class ServiceOfferView {

    @Id
    private Long id;

    private String enterpriseName;

    private long enterpriseId;

    private String subDomainName;

    private Long credentialId;

    private String subjectDid;

    private String name;

    private String label;

    private String producedBy;

    private String copyrightOwnedBy;

    private String description;

    private String accessType;

    private String requestType;

    private String formatType;

    private String terms;

    private String termsHash;

    @Transient
    private String offerLink;

    @Transient
    private Map<String, Object> meta;

    /**
     * Gets offer link.
     *
     * @return the offer link
     */
    public String getOfferLink() {
        return "https://" + this.subDomainName + "/.well-known/" + name + ".json";
    }
}
