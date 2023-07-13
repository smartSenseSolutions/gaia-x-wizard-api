/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Duplicate entity exception.
 */
public class DuplicateEntityException extends RuntimeException {

    private static final long serialVersionUID = 6695996323780675699L;

    /**
     * Instantiates a new Duplicate entity exception.
     */
    public DuplicateEntityException() {
    }

    /**
     * Instantiates a new Duplicate entity exception.
     *
     * @param message the message
     */
    public DuplicateEntityException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Duplicate entity exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Duplicate entity exception.
     *
     * @param cause the cause
     */
    public DuplicateEntityException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Duplicate entity exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public DuplicateEntityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
