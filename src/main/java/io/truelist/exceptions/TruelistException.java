package io.truelist.exceptions;

/**
 * Base exception for all Truelist SDK errors.
 */
public class TruelistException extends RuntimeException {

    private final int statusCode;

    /**
     * Creates a new TruelistException.
     *
     * @param message the error message
     */
    public TruelistException(String message) {
        super(message);
        this.statusCode = 0;
    }

    /**
     * Creates a new TruelistException with an HTTP status code.
     *
     * @param message    the error message
     * @param statusCode the HTTP status code
     */
    public TruelistException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Creates a new TruelistException with a cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public TruelistException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    /**
     * Returns the HTTP status code associated with this error, or 0 if not applicable.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}
