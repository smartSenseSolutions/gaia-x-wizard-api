/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository;

import eu.gaiax.wizard.dao.entity.EnterpriseCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The interface Enterprise credential repository.
 */
public interface EnterpriseCredentialRepository extends JpaRepository<EnterpriseCredential, Long> {
    /**
     * Gets by enterprise id.
     *
     * @param enterpriseId the enterprise id
     * @return the by enterprise id
     */
    List<EnterpriseCredential> getByEnterpriseId(long enterpriseId);

    /**
     * Gets by enterprise id and label.
     *
     * @param enterpriseId the enterprise id
     * @param participant  the participant
     * @return the by enterprise id and label
     */
    EnterpriseCredential getByEnterpriseIdAndLabel(long enterpriseId, String participant);
}
