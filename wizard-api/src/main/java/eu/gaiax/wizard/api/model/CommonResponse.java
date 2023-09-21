/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The type Common response.
 *
 * @param <T> the type parameter
 */
public class CommonResponse<T> implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * The Message.
     */
    protected String message;

    /**
     * The Error messages.
     */
    protected List<String> errorMessages;

    /**
     * The Status.
     */
    protected int status;

    /**
     * The Payload.
     */
    protected T payload;

    /**
     * Instantiates a new Common response.
     */
    public CommonResponse() {

    }

    /**
     * @param payload payload to return to the client
     */
    private CommonResponse(T payload) {
        this.payload = payload;
    }

    /**
     * Of common response.
     *
     * @param <T>     the type parameter
     * @param payload the payload
     * @return the common response
     */
    public static <T> CommonResponse<T> of(T payload) {
        return CommonResponse.builder(payload).status(200).build();
    }

    /**
     * Of common response.
     *
     * @param message the message
     * @return the common response
     */
    public static CommonResponse<Object> of(String message) {
        return CommonResponse.builder(new Object()).status(200).message(message).build();
    }

    /**
     * Of common response.
     *
     * @param <T>     the type parameter
     * @param payload the payload
     * @param msg     the msg
     * @return the common response
     */
    public static <T> CommonResponse<T> of(T payload, String msg) {
        return CommonResponse.builder(payload).message(msg).status(200).build();
    }

    /**
     * Of common response.
     *
     * @param <T>     the type parameter
     * @param key     the key
     * @param payload the payload
     * @param msg     the msg
     * @return the common response
     */
    public static <T> CommonResponse<Map<String, T>> of(String key, T payload, String msg) {
        Map<String, T> map = new HashMap<>(1);
        map.put(key, payload);
        return CommonResponse.builder(map).message(msg).status(200).build();
    }

    /**
     * Of common response.
     *
     * @param <T>     the type parameter
     * @param key     the key
     * @param payload the payload
     * @return the common response
     */
    public static <T> CommonResponse<Map<String, T>> of(String key, T payload) {
        Map<String, T> map = new HashMap<>(1);
        map.put(key, payload);
        return CommonResponse.builder(map).status(200).build();
    }

    /**
     * Builder builder.
     *
     * @param <T>     the type parameter
     * @param payload payload to return to the client
     * @return builder builder
     */
    public static <T> Builder<T> builder(T payload) {
        return new Builder<>(payload);
    }

    /**
     * Gets message.
     *
     * @return {@link String}
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets message.
     *
     * @param message will display on ui side
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets payload.
     *
     * @return T payload
     */
    public T getPayload() {
        return this.payload;
    }

    /**
     * Sets payload.
     *
     * @param payload payload to return to the client
     */
    public void setPayload(T payload) {
        this.payload = payload;
    }

    /**
     * Gets error messages.
     *
     * @return errors error messages
     */
    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    /**
     * Sets error messages.
     *
     * @param errorMessages multiple error message
     */
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }


    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
        return this.status;
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
     * The type Builder.
     *
     * @param <T> the type parameter
     * @author nitin
     */
    public static class Builder<T> {
        private final CommonResponse<T> response;

        /**
         * Instantiates a new Builder.
         *
         * @param payload payload to return to the client
         */
        public Builder(T payload) {
            this.response = new CommonResponse<>(payload);
        }

        /**
         * Message builder.
         *
         * @param message will display on ui side
         * @return Builder builder
         */
        public Builder<T> message(String message) {
            this.response.message = message;
            return this;
        }


        /**
         * Status builder.
         *
         * @param status API specific status code
         * @return Builder builder
         */
        public Builder<T> status(int status) {
            this.response.status = status;
            return this;
        }


        /**
         * Errors builder.
         *
         * @param errorMessages multiple error message
         * @return builder builder
         */
        public Builder<T> errors(List<String> errorMessages) {
            this.response.errorMessages = errorMessages;
            return this;
        }

        /**
         * Payload builder.
         *
         * @param payload payload to return to the client
         * @return builder builder
         */
        public Builder<T> payload(T payload) {
            this.response.payload = payload;
            return this;
        }

        /**
         * Build common response.
         *
         * @return {@link CommonResponse}
         */
        public CommonResponse<T> build() {
            return this.response;
        }
    }
}
