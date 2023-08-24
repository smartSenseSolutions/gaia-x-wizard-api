package eu.gaiax.wizard.api.exception;

public class RemoteServiceException extends RuntimeException {
    private static final long serialVersionUID = 1263029005410226031L;

    private int status = 500;

    /**
     * Instantiates a new Remote service  exception.
     */
    public RemoteServiceException() {
    }

    /**
     * Instantiates a new Remote service  exception.
     *
     * @param message the message
     */
    public RemoteServiceException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Remote service  exception.
     *
     * @param status  http status
     * @param message the message
     */
    public RemoteServiceException(int status, String message) {
        super(message);
        this.status = status;
    }


    /**
     * Instantiates a new Remote service  exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Remote service exception.
     *
     * @param cause the cause
     */
    public RemoteServiceException(Throwable cause) {
        super(cause);
    }

    public int getStatus() {
        return this.status;
    }
}
