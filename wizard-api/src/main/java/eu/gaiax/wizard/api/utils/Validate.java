/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.utils;

import eu.gaiax.wizard.api.exception.BadDataException;

import java.util.Objects;
import java.util.function.Function;

/**
 * The type Validate.
 *
 * @param <T> the type parameter
 */
public class Validate<T> {
    private T value;
    private boolean match = false;

    private Validate() {
    }

    private Validate(T value) {
        this.value = value;
    }

    /**
     * Value validate.
     *
     * @param <V>   the type parameter
     * @param value the value
     * @return the validate
     */
    public static <V> Validate<V> value(V value) {
        return new Validate<>(value);
    }

    /**
     * Is true validate.
     *
     * @param <V>       the type parameter
     * @param condition the condition
     * @return the validate
     */
    public static <V> Validate<V> isTrue(boolean condition) {
        Validate<V> validate = new Validate<>();
        if (condition) {
            validate.match = true;
        }
        return validate;
    }

    /**
     * Throws if {@code condition} is false
     *
     * @param <V>       the type parameter
     * @param condition the condition
     * @return validate validate
     */
    public static <V> Validate<V> isFalse(boolean condition) {
        Validate<V> validate = new Validate<>();
        if (!condition) {
            validate.match = true;
        }
        return validate;
    }

    /**
     * Is null validate.
     *
     * @param <T>   the type parameter
     * @param value the value
     * @return the validate
     */
    public static <T> Validate<T> isNull(T value) {
        return new Validate<>(value).isNull();
    }

    /**
     * Is not null validate.
     *
     * @param <T>   the type parameter
     * @param value the value
     * @return the validate
     */
    public static <T> Validate<T> isNotNull(T value) {
        return new Validate<>(value).isNotNull();
    }

    /**
     * In length validate.
     *
     * @param min the min
     * @param max the max
     * @return the validate
     */
    public Validate<T> inLength(int min, int max) {
        if (Objects.isNull(value)) {
            return this;
        }
        if (match || value.toString().length() < min && value.toString().length() > max) {
            match = true;
        }
        return this;
    }

    /**
     * Is not empty validate.
     *
     * @return the validate
     */
    public Validate<T> isNotEmpty() {
        if (match || Objects.isNull(value) || String.valueOf(value).trim().isEmpty()) {
            match = true;
        }
        return this;
    }

    /**
     * Is null validate.
     *
     * @return the validate
     */
    public Validate<T> isNull() {
        if (match || Objects.isNull(value)) {
            match = true;
        }
        return this;
    }

    /**
     * Is not null validate.
     *
     * @return the validate
     */
    public Validate<T> isNotNull() {
        if (match || !Objects.isNull(value)) {
            match = true;
        }
        return this;
    }

    /**
     * Check validate.
     *
     * @param checkFunction the check function
     * @return the validate
     */
    public Validate<T> check(Function<T, Boolean> checkFunction) {
        if (match || checkFunction.apply(value)) {
            match = true;
        }
        return this;
    }

    /**
     * Check not validate.
     *
     * @param checkFunction the check function
     * @return the validate
     */
    public Validate<T> checkNot(Function<T, Boolean> checkFunction) {
        if (match || !checkFunction.apply(value)) {
            match = true;
        }
        return this;
    }

    /**
     * Throw passed exception if expression is match
     *
     * @param e exception to throw
     * @return the t
     */
    public T launch(RuntimeException e) {
        if (match) {
            throw e;
        }
        return value;
    }

    /**
     * Throw {@code BadDataException} if expression is match with passed message
     *
     * @param message exception message
     * @return the t
     */
    public T launch(String message) {
        if (match) {
            throw new BadDataException(message);
        }
        return value;
    }

    /**
     * Calculate all of the conditions are true or not
     *
     * @return true if any of condition are true
     */
    public boolean calculate() {
        return match;
    }
}
