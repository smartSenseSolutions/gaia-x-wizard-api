package eu.gaiax.wizard.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException() {
        super();
    }

    public ParticipantNotFoundException(String message) {
        super(message);
    }

    public ParticipantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParticipantNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ParticipantNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
