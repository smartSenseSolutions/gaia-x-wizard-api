/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Entity creation exception.
 */
public class EntityCreationException extends RuntimeException {

    private static final long serialVersionUID = -7358043925880048999L;

    /**
     * Instantiates a new Entity creation exception.
     */
    public EntityCreationException() {
    }

    /**
     * Instantiates a new Entity creation exception.
     *
     * @param message the message
     */
    public EntityCreationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Entity creation exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EntityCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Entity creation exception.
     *
     * @param cause the cause
     */
    public EntityCreationException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Entity creation exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public EntityCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
