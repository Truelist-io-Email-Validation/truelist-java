package io.truelist;

import com.google.gson.Gson;
import io.truelist.exceptions.ApiException;
import io.truelist.exceptions.AuthenticationException;
import io.truelist.exceptions.RateLimitException;
import io.truelist.exceptions.TruelistException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Main client for the Truelist email validation API.
 *
 * <p>Create instances using the builder pattern:</p>
 * <pre>{@code
 * Truelist client = Truelist.builder("your-api-key")
 *     .baseUrl("https://api.truelist.io")
 *     .timeout(Duration.ofSeconds(10))
 *     .maxRetries(2)
 *     .build();
 *
 * ValidationResult result = client.validate("user@example.com");
 * }</pre>
 *
 * <p>The client is thread-safe and can be shared across threads.</p>
 */
public class Truelist {

    private static final String DEFAULT_BASE_URL = "https://api.truelist.io";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_RETRIES = 2;

    private final TruelistConfig config;
    private final HttpClient httpClient;
    private final Gson gson;

    private Truelist(TruelistConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getTimeout())
                .build();
        this.gson = new Gson();
    }

    // Visible for testing
    Truelist(TruelistConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    /**
     * Creates a new builder for constructing a Truelist client.
     *
     * @param apiKey the server API key for authentication
     * @return a new builder instance
     * @throws IllegalArgumentException if apiKey is null or blank
     */
    public static Builder builder(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key must not be null or blank");
        }
        return new Builder(apiKey);
    }

    /**
     * Validates an email address.
     *
     * @param email the email address to validate
     * @return the validation result
     * @throws AuthenticationException if the API key is invalid (401)
     * @throws RateLimitException      if the rate limit is exceeded (429)
     * @throws ApiException            if the API returns an unexpected error
     * @throws TruelistException       if a network or other error occurs
     * @throws IllegalArgumentException if email is null or blank
     */
    public ValidationResult validate(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String responseBody = executePostWithRetry(
                "/api/v1/verify_inline?email=" + encodedEmail,
                config.getApiKey()
        );

        VerifyResponse wrapper = gson.fromJson(responseBody, VerifyResponse.class);
        if (wrapper.emails == null || wrapper.emails.isEmpty()) {
            throw new ApiException("No results returned from API", 0);
        }
        return wrapper.emails.get(0);
    }

    /**
     * Retrieves account information for the authenticated user.
     *
     * @return the account info
     * @throws AuthenticationException if the API key is invalid (401)
     * @throws RateLimitException      if the rate limit is exceeded (429)
     * @throws ApiException            if the API returns an unexpected error
     * @throws TruelistException       if a network or other error occurs
     */
    public AccountInfo getAccount() {
        String responseBody = executeGetWithRetry("/me", config.getApiKey());
        return gson.fromJson(responseBody, AccountInfo.class);
    }

    /**
     * Returns the configuration used by this client.
     *
     * @return the configuration
     */
    public TruelistConfig getConfig() {
        return config;
    }

    /** Wrapper for the verify response envelope. */
    private static class VerifyResponse {
        List<ValidationResult> emails;
    }

    private String executePostWithRetry(String path, String token) {
        int attempts = 0;
        int maxAttempts = config.getMaxRetries() + 1;

        while (true) {
            attempts++;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.getBaseUrl() + path))
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/json")
                        .timeout(config.getTimeout())
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                handleErrorResponse(response, attempts, maxAttempts);
                return response.body();

            } catch (AuthenticationException e) {
                throw e;
            } catch (RateLimitException e) {
                if (attempts >= maxAttempts) {
                    throw e;
                }
                sleep(attempts);
            } catch (ApiException e) {
                if (!isRetryableStatus(e.getStatusCode()) || attempts >= maxAttempts) {
                    throw e;
                }
                sleep(attempts);
            } catch (IOException e) {
                if (attempts >= maxAttempts) {
                    throw new TruelistException("Request failed after " + attempts + " attempts", e);
                }
                sleep(attempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TruelistException("Request interrupted", e);
            }
        }
    }

    private String executeGetWithRetry(String path, String token) {
        int attempts = 0;
        int maxAttempts = config.getMaxRetries() + 1;

        while (true) {
            attempts++;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.getBaseUrl() + path))
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/json")
                        .timeout(config.getTimeout())
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                handleErrorResponse(response, attempts, maxAttempts);
                return response.body();

            } catch (AuthenticationException e) {
                throw e;
            } catch (RateLimitException e) {
                if (attempts >= maxAttempts) {
                    throw e;
                }
                sleep(attempts);
            } catch (ApiException e) {
                if (!isRetryableStatus(e.getStatusCode()) || attempts >= maxAttempts) {
                    throw e;
                }
                sleep(attempts);
            } catch (IOException e) {
                if (attempts >= maxAttempts) {
                    throw new TruelistException("Request failed after " + attempts + " attempts", e);
                }
                sleep(attempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TruelistException("Request interrupted", e);
            }
        }
    }

    private void handleErrorResponse(HttpResponse<String> response, int attempts, int maxAttempts) {
        int status = response.statusCode();

        if (status >= 200 && status < 300) {
            return;
        }

        if (status == 401) {
            throw new AuthenticationException(
                    "Authentication failed: invalid API key (HTTP 401)");
        }

        if (status == 429) {
            throw new RateLimitException(
                    "Rate limit exceeded (HTTP 429). Please slow down your requests.");
        }

        if (status >= 500) {
            throw new ApiException(
                    "Server error (HTTP " + status + "): " + response.body(), status);
        }

        throw new ApiException(
                "Unexpected response (HTTP " + status + "): " + response.body(), status);
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode >= 500;
    }

    private void sleep(int attempt) {
        try {
            long delay = (long) Math.pow(2, attempt - 1) * 100;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Builder for constructing {@link Truelist} client instances.
     */
    public static class Builder {

        private final String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = DEFAULT_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;

        private Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Sets the base URL for the API.
         *
         * @param baseUrl the base URL (default: https://api.truelist.io)
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Base URL must not be null or blank");
            }
            // Strip trailing slash
            this.baseUrl = baseUrl.endsWith("/")
                    ? baseUrl.substring(0, baseUrl.length() - 1)
                    : baseUrl;
            return this;
        }

        /**
         * Sets the request timeout.
         *
         * @param timeout the timeout duration (default: 30 seconds)
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            if (timeout == null) {
                throw new IllegalArgumentException("Timeout must not be null");
            }
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the maximum number of retries for transient errors (429, 5xx, network errors).
         *
         * <p>Authentication errors (401) and other 4xx client errors are never retried.</p>
         *
         * @param maxRetries the max retries (default: 2)
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("Max retries must be non-negative");
            }
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Builds and returns a new {@link Truelist} client instance.
         *
         * @return the configured client
         */
        public Truelist build() {
            TruelistConfig config = new TruelistConfig(
                    apiKey, baseUrl, timeout, maxRetries);
            return new Truelist(config);
        }
    }
}
