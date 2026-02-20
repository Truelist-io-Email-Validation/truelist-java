package io.truelist;

import java.time.Duration;

/**
 * Configuration for the Truelist client.
 *
 * <p>Use {@link Truelist#builder(String)} to create instances.</p>
 */
public class TruelistConfig {

    private final String apiKey;
    private final String formApiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;

    TruelistConfig(String apiKey, String formApiKey, String baseUrl,
                   Duration timeout, int maxRetries) {
        this.apiKey = apiKey;
        this.formApiKey = formApiKey;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    /**
     * Returns the server API key.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the form API key for frontend validation, or null if not set.
     *
     * @return the form API key
     */
    public String getFormApiKey() {
        return formApiKey;
    }

    /**
     * Returns the base URL for the API.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the request timeout.
     *
     * @return the timeout duration
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Returns the maximum number of retries for transient errors.
     *
     * @return the max retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }
}
