/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import java.util.Map;

/**
 * The type Validation error response.
 */
public class ValidationErrorResponse extends ErrorResponse {

    private Map<String, String> fieldErrors;

    /**
     * Instantiates a new Validation error response.
     */
    public ValidationErrorResponse() {
    }

    /**
     * Instantiates a new Validation error response.
     *
     * @param fieldErrors the field errors
     * @param status      the status
     * @param message     the message
     */
    public ValidationErrorResponse(Map<String, String> fieldErrors, int status, String message) {
        super(message, status);
        this.fieldErrors = fieldErrors;
    }

    /**
     * Gets field errors.
     *
     * @return the field errors
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * Sets field errors.
     *
     * @param fieldErrors the field errors
     */
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

}
