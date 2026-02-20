package io.truelist.exceptions;

/**
 * Thrown when the API returns a 401 Unauthorized response.
 * This exception is always thrown on authentication failures, regardless of configuration.
 */
public class AuthenticationException extends TruelistException {

    /**
     * Creates a new AuthenticationException.
     *
     * @param message the error message
     */
    public AuthenticationException(String message) {
        super(message, 401);
    }
}
