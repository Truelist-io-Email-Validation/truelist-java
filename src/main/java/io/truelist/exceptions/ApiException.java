package io.truelist.exceptions;

/**
 * Thrown when the API returns an unexpected error response (5xx, etc.).
 */
public class ApiException extends TruelistException {

    /**
     * Creates a new ApiException.
     *
     * @param message    the error message
     * @param statusCode the HTTP status code
     */
    public ApiException(String message, int statusCode) {
        super(message, statusCode);
    }
}
