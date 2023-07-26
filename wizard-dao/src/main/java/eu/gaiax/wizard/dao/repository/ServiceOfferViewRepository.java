/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository;

import eu.gaiax.wizard.dao.entity.ServiceOfferView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Service offer view repository.
 */
@Repository
public interface ServiceOfferViewRepository extends JpaRepository<ServiceOfferView, Long> {
    /**
     * Gets by enterprise id.
     *
     * @param enterpriseId the enterprise id
     * @return the by enterprise id
     */
    List<ServiceOfferView> getByEnterpriseId(long enterpriseId);

    /**
     * Gets all service offers.
     *
     * @param enterpriseId the enterprise id
     * @return the all service offers
     */
    @Query("from ServiceOfferView where enterpriseId <> :enterpriseId")
    List<ServiceOfferView> getAllServiceOffers(@Param("enterpriseId") long enterpriseId);

    /**
     * Gets by enterprise id and id.
     *
     * @param enterpriseId the enterprise id
     * @param id           the id
     * @return the by enterprise id and id
     */
    ServiceOfferView getByEnterpriseIdAndId(long enterpriseId, long id);
}
