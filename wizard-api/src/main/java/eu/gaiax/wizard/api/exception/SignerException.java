/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.exception;

/**
 * The type Feign client exception.
 */
public class SignerException extends RuntimeException {
    private static final long serialVersionUID = 3057339862453629449L;

    private int status = 500;

    /**
     * Instantiates a new AIML service  exception.
     */
    public SignerException() {
    }

    /**
     * Instantiates a new AIML service exception.
     *
     * @param e Remote Service Exception
     */
    public SignerException(RemoteServiceException e) {
        super(e.getMessage());
        this.status = e.getStatus();
    }


    /**
     * Instantiates a new AIML service  exception.
     *
     * @param message the message
     */
    public SignerException(String message) {
        super(message);
    }

    /**
     * Instantiates a new AIML service  exception.
     *
     * @param status  http status
     * @param message the message
     */
    public SignerException(int status, String message) {
        super(message);
        this.status = status;
    }


    /**
     * Instantiates a new AIML service  exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public SignerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new AIML service exception.
     *
     * @param cause the cause
     */
    public SignerException(Throwable cause) {
        super(cause);
    }

    public int getStatus() {
        return this.status;
    }
}
