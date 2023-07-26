package eu.gaiax.wizard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VaultException extends RuntimeException {
    public VaultException(String message, Throwable cause) {
        super(message, cause);
    }
}
