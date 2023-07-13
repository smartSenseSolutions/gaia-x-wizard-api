/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Bad data exception.
 */
public class BadDataException extends RuntimeException {

    private static final long serialVersionUID = -7554292655385788149L;

    /**
     * Instantiates a new Bad data exception.
     */
    public BadDataException() {
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param message the message
     */
    public BadDataException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BadDataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param cause the cause
     */
    public BadDataException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public BadDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
