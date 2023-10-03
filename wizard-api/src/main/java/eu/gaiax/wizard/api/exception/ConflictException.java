/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Bad data exception.
 */
public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = -1472125906008931370L;

    /**
     * Instantiates a new Bad data exception.
     */
    public ConflictException() {
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param message the message
     */
    public ConflictException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Bad data exception.
     *
     * @param cause the cause
     */
    public ConflictException(Throwable cause) {
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
    public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
