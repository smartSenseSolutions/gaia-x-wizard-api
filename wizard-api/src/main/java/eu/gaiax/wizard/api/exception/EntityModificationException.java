/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Entity modification exception.
 */
public class EntityModificationException extends RuntimeException {

    private static final long serialVersionUID = 756532366516638568L;

    /**
     * Instantiates a new Entity modification exception.
     */
    public EntityModificationException() {
    }

    /**
     * Instantiates a new Entity modification exception.
     *
     * @param message the message
     */
    public EntityModificationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Entity modification exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EntityModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Entity modification exception.
     *
     * @param cause the cause
     */
    public EntityModificationException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Entity modification exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public EntityModificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
