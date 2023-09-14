package eu.gaiax.wizard.api.exception;

public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException() {
    }

    public ForbiddenAccessException(String message) {
        super(message);
    }
}
