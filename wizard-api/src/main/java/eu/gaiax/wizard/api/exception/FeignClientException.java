/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Feign client exception.
 */
public class FeignClientException extends Exception {

    private static final long serialVersionUID = -8823619466281236169L;

    /**
     * Instantiates a new Feign client exception.
     */
    public FeignClientException() {
    }

    /**
     * Instantiates a new Feign client exception.
     *
     * @param message the message
     */
    public FeignClientException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Feign client exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public FeignClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Feign client exception.
     *
     * @param cause the cause
     */
    public FeignClientException(Throwable cause) {
        super(cause);
    }
}
