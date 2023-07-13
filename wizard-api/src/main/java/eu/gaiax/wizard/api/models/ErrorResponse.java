/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models;

/**
 * The type Error response.
 */
public class ErrorResponse {
    private String message;

    private int status;

    private long timeStamp;

    /**
     * Instantiates a new Error response.
     */
    public ErrorResponse() {
    }

    /**
     * Instantiates a new Error response.
     *
     * @param message the message
     * @param status  the status
     */
    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        timeStamp = System.currentTimeMillis();
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Gets time stamp.
     *
     * @return the time stamp
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets time stamp.
     *
     * @param timeStamp the time stamp
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
