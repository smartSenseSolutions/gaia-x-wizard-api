/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.repository;


import eu.gaiax.wizard.dao.entity.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Enterprise repository.
 */
@Repository
public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    /**
     * Exists by legal name boolean.
     *
     * @param legalName the legal name
     * @return the boolean
     */
    boolean existsByLegalName(String legalName);

    /**
     * Exists by email boolean.
     *
     * @param email the email
     * @return the boolean
     */
    boolean existsByEmail(String email);

    /**
     * Exists by sub domain name boolean.
     *
     * @param subDomain the sub domain
     * @return the boolean
     */
    boolean existsBySubDomainName(String subDomain);

    /**
     * Exists by legal registration number boolean.
     *
     * @param registrationNumber the registration number
     * @return the boolean
     */
    boolean existsByLegalRegistrationNumber(String registrationNumber);

    /**
     * Gets by sub domain name.
     *
     * @param hostName the host name
     * @return the by sub domain name
     */
    Enterprise getBySubDomainName(String hostName);

    /**
     * Gets by email.
     *
     * @param email the email
     * @return the by email
     */
    Enterprise getByEmail(String email);
}
