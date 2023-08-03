/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

/**
 * The enum Registration status.
 */
public enum RegistrationStatus {

    /**
     * Started registration status.
     */
    STARTED(1),

    /**
     * Domain created registration status.
     */
    DOMAIN_CREATED(2),

    /**
     * Domain creation failed registration status.
     */
    DOMAIN_CREATION_FAILED(3),

    /**
     * Certificate created registration status.
     */
    CERTIFICATE_CREATED(4),
    CERTIFICATE_CREATION_IN_PROCESS(12),

    /**
     * Certificate creation failed registration status.
     */
    CERTIFICATE_CREATION_FAILED(5),

    /**
     * Ingress created registration status.
     */
    INGRESS_CREATED(6),

    /**
     * Ingress creation failed registration status.
     */
    INGRESS_CREATION_FAILED(7),

    /**
     * Did json created registration status.
     */
    DID_JSON_CREATED(8),

    /**
     * Did json creation failed registration status.
     */
    DID_JSON_CREATION_FAILED(9),

    /**
     * Participant json created registration status.
     */
    PARTICIPANT_JSON_CREATED(10),
    /**
     * Participant json creation failed registration status.
     */
    PARTICIPANT_JSON_CREATION_FAILED(11);

    private final int status;

    RegistrationStatus(int status) {
        this.status = status;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
        return this.status;
    }
}
