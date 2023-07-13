/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository;

import eu.gaiax.wizard.dao.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Service offer repository.
 */
@Repository

public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
    /**
     * Gets by enterprise id.
     *
     * @param enterpriseId the enterprise id
     * @return the by enterprise id
     */
    List<ServiceOffer> getByEnterpriseId(long enterpriseId);

    /**
     * Gets by id and enterprise id.
     *
     * @param offerId      the offer id
     * @param enterpriseId the enterprise id
     * @return the by id and enterprise id
     */
    ServiceOffer getByIdAndEnterpriseId(long offerId, long enterpriseId);

    /**
     * Gets by enterprise id and name.
     *
     * @param enterpriseId the enterprise id
     * @param name         the name
     * @return the by enterprise id and name
     */
    ServiceOffer getByEnterpriseIdAndName(long enterpriseId, String name);
}
