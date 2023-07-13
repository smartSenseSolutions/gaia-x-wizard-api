/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository;

import eu.gaiax.wizard.dao.entity.EnterpriseCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Enterprise certificate repository.
 */
public interface EnterpriseCertificateRepository extends JpaRepository<EnterpriseCertificate, Long> {

    /**
     * Gets by enterprise id.
     *
     * @param enterpriseId the enterprise id
     * @return the by enterprise id
     */
    EnterpriseCertificate getByEnterpriseId(long enterpriseId);

}
