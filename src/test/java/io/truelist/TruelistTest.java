package io.truelist;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.truelist.exceptions.ApiException;
import io.truelist.exceptions.AuthenticationException;
import io.truelist.exceptions.RateLimitException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class TruelistTest {

    private WireMockServer wireMock;
    private Truelist client;

    private static final String VALID_RESPONSE = "{\"emails\":[{" +
            "\"address\":\"user@example.com\"," +
            "\"domain\":\"example.com\"," +
            "\"canonical\":\"user\"," +
            "\"mx_record\":null," +
            "\"first_name\":null," +
            "\"last_name\":null," +
            "\"email_state\":\"ok\"," +
            "\"email_sub_state\":\"email_ok\"," +
            "\"verified_at\":\"2026-02-21T10:00:00.000Z\"," +
            "\"did_you_mean\":null}]}";

    private static final String INVALID_RESPONSE = "{\"emails\":[{" +
            "\"address\":\"bad@example.com\"," +
            "\"domain\":\"example.com\"," +
            "\"canonical\":\"bad\"," +
            "\"mx_record\":null," +
            "\"first_name\":null," +
            "\"last_name\":null," +
            "\"email_state\":\"email_invalid\"," +
            "\"email_sub_state\":\"failed_smtp_check\"," +
            "\"verified_at\":\"2026-02-21T10:00:00.000Z\"," +
            "\"did_you_mean\":null}]}";

    private static final String DISPOSABLE_RESPONSE = "{\"emails\":[{" +
            "\"address\":\"user@tempmail.com\"," +
            "\"domain\":\"tempmail.com\"," +
            "\"canonical\":\"user\"," +
            "\"mx_record\":null," +
            "\"first_name\":null," +
            "\"last_name\":null," +
            "\"email_state\":\"ok\"," +
            "\"email_sub_state\":\"is_disposable\"," +
            "\"verified_at\":\"2026-02-21T10:00:00.000Z\"," +
            "\"did_you_mean\":null}]}";

    private static final String ACCOUNT_RESPONSE = "{" +
            "\"email\":\"team@company.com\"," +
            "\"name\":\"Team Lead\"," +
            "\"uuid\":\"a3828d19-1234-5678-9abc-def012345678\"," +
            "\"time_zone\":\"America/New_York\"," +
            "\"is_admin_role\":true," +
            "\"token\":\"test_token\"," +
            "\"api_keys\":[]," +
            "\"account\":{\"name\":\"Company Inc\",\"payment_plan\":\"pro\",\"users\":[]}}";

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        client = Truelist.builder("test-api-key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(0)
                .build();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    // --- Builder Tests ---

    @Test
    void builderRejectsNullApiKey() {
        assertThrows(IllegalArgumentException.class, () -> Truelist.builder(null));
    }

    @Test
    void builderRejectsBlankApiKey() {
        assertThrows(IllegalArgumentException.class, () -> Truelist.builder("  "));
    }

    @Test
    void builderSetsDefaults() {
        Truelist defaultClient = Truelist.builder("key").build();
        TruelistConfig config = defaultClient.getConfig();

        assertEquals("https://api.truelist.io", config.getBaseUrl());
        assertEquals(Duration.ofSeconds(30), config.getTimeout());
        assertEquals(2, config.getMaxRetries());
    }

    @Test
    void builderConfiguresAllOptions() {
        Truelist customClient = Truelist.builder("my-key")
                .baseUrl("https://custom.api.com")
                .timeout(Duration.ofSeconds(15))
                .maxRetries(5)
                .build();

        TruelistConfig config = customClient.getConfig();
        assertEquals("my-key", config.getApiKey());
        assertEquals("https://custom.api.com", config.getBaseUrl());
        assertEquals(Duration.ofSeconds(15), config.getTimeout());
        assertEquals(5, config.getMaxRetries());
    }

    @Test
    void builderStripsTrailingSlashFromBaseUrl() {
        Truelist c = Truelist.builder("key")
                .baseUrl("https://api.truelist.io/")
                .build();
        assertEquals("https://api.truelist.io", c.getConfig().getBaseUrl());
    }

    @Test
    void builderRejectsNegativeMaxRetries() {
        assertThrows(IllegalArgumentException.class, () ->
                Truelist.builder("key").maxRetries(-1));
    }

    // --- validate() Tests ---

    @Test
    void validateReturnsValidResult() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .withQueryParam("email", equalTo("user@example.com"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .willReturn(okJson(VALID_RESPONSE)));

        ValidationResult result = client.validate("user@example.com");

        assertEquals("user@example.com", result.getEmail());
        assertEquals("example.com", result.getDomain());
        assertEquals("user", result.getCanonical());
        assertEquals("ok", result.getState());
        assertEquals("email_ok", result.getSubState());
        assertNull(result.getSuggestion());
        assertTrue(result.isValid());
        assertFalse(result.isInvalid());
        assertFalse(result.isAcceptAll());
        assertFalse(result.isUnknown());
    }

    @Test
    void validateReturnsInvalidResult() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(okJson(INVALID_RESPONSE)));

        ValidationResult result = client.validate("bad@example.com");

        assertEquals("email_invalid", result.getState());
        assertEquals("failed_smtp_check", result.getSubState());
        assertTrue(result.isInvalid());
        assertFalse(result.isValid());
    }

    @Test
    void validateReturnsDisposableResult() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(okJson(DISPOSABLE_RESPONSE)));

        ValidationResult result = client.validate("user@tempmail.com");

        assertEquals("ok", result.getState());
        assertEquals("is_disposable", result.getSubState());
        assertTrue(result.isDisposable());
        assertFalse(result.isRole());
    }

    @Test
    void validateReturnsAcceptAllResult() {
        String acceptAllResponse = "{\"emails\":[{" +
                "\"address\":\"user@catchall.com\"," +
                "\"domain\":\"catchall.com\"," +
                "\"canonical\":\"user\"," +
                "\"mx_record\":null," +
                "\"first_name\":null," +
                "\"last_name\":null," +
                "\"email_state\":\"accept_all\"," +
                "\"email_sub_state\":\"email_ok\"," +
                "\"verified_at\":\"2026-02-21T10:00:00.000Z\"," +
                "\"did_you_mean\":null}]}";

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(okJson(acceptAllResponse)));

        ValidationResult result = client.validate("user@catchall.com");

        assertEquals("accept_all", result.getState());
        assertTrue(result.isAcceptAll());
        assertFalse(result.isValid());
    }

    @Test
    void validateReturnsUnknownResult() {
        String unknownResponse = "{\"emails\":[{" +
                "\"address\":\"user@mystery.com\"," +
                "\"domain\":\"mystery.com\"," +
                "\"canonical\":\"user\"," +
                "\"mx_record\":null," +
                "\"first_name\":null," +
                "\"last_name\":null," +
                "\"email_state\":\"unknown\"," +
                "\"email_sub_state\":\"unknown_error\"," +
                "\"verified_at\":\"2026-02-21T10:00:00.000Z\"," +
                "\"did_you_mean\":null}]}";

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(okJson(unknownResponse)));

        ValidationResult result = client.validate("user@mystery.com");

        assertEquals("unknown", result.getState());
        assertTrue(result.isUnknown());
        assertFalse(result.isValid());
    }

    @Test
    void validateSendsEmailAsQueryParam() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .withQueryParam("email", equalTo("test@example.com"))
                .willReturn(okJson(VALID_RESPONSE)));

        client.validate("test@example.com");

        wireMock.verify(postRequestedFor(urlPathEqualTo("/api/v1/verify_inline"))
                .withQueryParam("email", equalTo("test@example.com")));
    }

    @Test
    void validateRejectsNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> client.validate(null));
    }

    @Test
    void validateRejectsBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> client.validate("  "));
    }

    @Test
    void validateReturnsSuggestion() {
        String suggestionResponse = "{\"emails\":[{" +
                "\"address\":\"user@gmial.com\"," +
                "\"domain\":\"gmial.com\"," +
                "\"canonical\":\"user\"," +
                "\"mx_record\":null," +
                "\"first_name\":null," +
                "\"last_name\":null," +
                "\"email_state\":\"email_invalid\"," +
                "\"email_sub_state\":\"unknown_error\"," +
                "\"verified_at\":\"2026-02-21T10:00:00.000Z\"," +
                "\"did_you_mean\":\"user@gmail.com\"}]}";

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(okJson(suggestionResponse)));

        ValidationResult result = client.validate("user@gmial.com");

        assertEquals("user@gmail.com", result.getSuggestion());
    }

    // --- getAccount() Tests ---

    @Test
    void getAccountReturnsAccountInfo() {
        wireMock.stubFor(get(urlEqualTo("/me"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .willReturn(okJson(ACCOUNT_RESPONSE)));

        AccountInfo account = client.getAccount();

        assertEquals("team@company.com", account.getEmail());
        assertEquals("Team Lead", account.getName());
        assertEquals("a3828d19-1234-5678-9abc-def012345678", account.getUuid());
        assertEquals("America/New_York", account.getTimeZone());
        assertTrue(account.isAdminRole());
        assertNotNull(account.getAccount());
        assertEquals("Company Inc", account.getAccount().getName());
        assertEquals("pro", account.getAccount().getPaymentPlan());
        assertEquals("pro", account.getPlan());
    }

    // --- Error Handling Tests ---

    @Test
    void authErrorAlwaysThrowsOnValidate() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                client.validate("user@example.com"));

        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void authErrorAlwaysThrowsOnGetAccount() {
        wireMock.stubFor(get(urlEqualTo("/me"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        assertThrows(AuthenticationException.class, () -> client.getAccount());
    }

    @Test
    void rateLimitThrows429Exception() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(aResponse().withStatus(429).withBody("Too Many Requests")));

        RateLimitException ex = assertThrows(RateLimitException.class, () ->
                client.validate("user@example.com"));

        assertEquals(429, ex.getStatusCode());
    }

    @Test
    void serverErrorThrowsApiException() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(serverError().withBody("Internal Server Error")));

        ApiException ex = assertThrows(ApiException.class, () ->
                client.validate("user@example.com"));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void unexpectedStatusThrowsApiException() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(aResponse().withStatus(403).withBody("Forbidden")));

        ApiException ex = assertThrows(ApiException.class, () ->
                client.validate("user@example.com"));

        assertEquals(403, ex.getStatusCode());
    }

    // --- Retry Tests ---

    @Test
    void retriesOnServerError() {
        Truelist retryClient = Truelist.builder("test-api-key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(serverError().withBody("Error"))
                .willSetStateTo("first-retry"));

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .inScenario("retry")
                .whenScenarioStateIs("first-retry")
                .willReturn(serverError().withBody("Error"))
                .willSetStateTo("second-retry"));

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .inScenario("retry")
                .whenScenarioStateIs("second-retry")
                .willReturn(okJson(VALID_RESPONSE)));

        ValidationResult result = retryClient.validate("user@example.com");

        assertEquals("ok", result.getState());
        wireMock.verify(3, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }

    @Test
    void doesNotRetryAuthErrors() {
        Truelist retryClient = Truelist.builder("bad-key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        assertThrows(AuthenticationException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }

    @Test
    void retriesOnRateLimitError() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .inScenario("rate-limit-retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(429).withBody("Rate limited"))
                .willSetStateTo("first-retry"));

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .inScenario("rate-limit-retry")
                .whenScenarioStateIs("first-retry")
                .willReturn(okJson(VALID_RESPONSE)));

        ValidationResult result = retryClient.validate("user@example.com");

        assertEquals("ok", result.getState());
        wireMock.verify(2, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }

    @Test
    void exhaustsRetriesOnPersistentRateLimitError() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(1)
                .build();

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(aResponse().withStatus(429).withBody("Rate limited")));

        assertThrows(RateLimitException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(2, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }

    @Test
    void doesNotRetryClientErrors() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(aResponse().withStatus(403).withBody("Forbidden")));

        assertThrows(ApiException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }

    @Test
    void exhaustsRetriesOnPersistentServerError() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(1)
                .build();

        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(serverError().withBody("Error")));

        assertThrows(ApiException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(2, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }

    @Test
    void zeroRetriesMeansOneAttempt() {
        wireMock.stubFor(post(urlPathEqualTo("/api/v1/verify_inline"))
                .willReturn(serverError().withBody("Error")));

        assertThrows(ApiException.class, () -> client.validate("user@example.com"));

        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/api/v1/verify_inline")));
    }
}
