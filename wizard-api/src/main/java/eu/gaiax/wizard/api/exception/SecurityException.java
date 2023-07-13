/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Security exception.
 */
public class SecurityException extends RuntimeException {


    private static final long serialVersionUID = 9123274635965425256L;

    /**
     * Instantiates a new Security exception.
     */
    public SecurityException() {
    }

    /**
     * Instantiates a new Security exception.
     *
     * @param message the message
     */
    public SecurityException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Security exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Security exception.
     *
     * @param cause the cause
     */
    public SecurityException(Throwable cause) {
        super(cause);
    }
}
