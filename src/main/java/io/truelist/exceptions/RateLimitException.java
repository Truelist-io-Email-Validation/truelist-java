package io.truelist.exceptions;

/**
 * Thrown when the API returns a 429 Too Many Requests response.
 */
public class RateLimitException extends TruelistException {

    /**
     * Creates a new RateLimitException.
     *
     * @param message the error message
     */
    public RateLimitException(String message) {
        super(message, 429);
    }
}
