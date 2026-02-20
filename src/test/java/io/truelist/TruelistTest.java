package io.truelist;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.truelist.exceptions.ApiException;
import io.truelist.exceptions.AuthenticationException;
import io.truelist.exceptions.RateLimitException;
import io.truelist.exceptions.TruelistException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class TruelistTest {

    private WireMockServer wireMock;
    private Truelist client;

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
        assertNull(config.getFormApiKey());
    }

    @Test
    void builderConfiguresAllOptions() {
        Truelist customClient = Truelist.builder("my-key")
                .baseUrl("https://custom.api.com")
                .timeout(Duration.ofSeconds(15))
                .maxRetries(5)
                .formApiKey("form-key")
                .build();

        TruelistConfig config = customClient.getConfig();
        assertEquals("my-key", config.getApiKey());
        assertEquals("https://custom.api.com", config.getBaseUrl());
        assertEquals(Duration.ofSeconds(15), config.getTimeout());
        assertEquals(5, config.getMaxRetries());
        assertEquals("form-key", config.getFormApiKey());
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
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(okJson("{" +
                        "\"state\":\"valid\"," +
                        "\"sub_state\":\"ok\"," +
                        "\"suggestion\":null," +
                        "\"free_email\":true," +
                        "\"role\":false," +
                        "\"disposable\":false" +
                        "}")));

        ValidationResult result = client.validate("user@gmail.com");

        assertEquals("valid", result.getState());
        assertEquals("ok", result.getSubState());
        assertNull(result.getSuggestion());
        assertTrue(result.isValid());
        assertFalse(result.isInvalid());
        assertFalse(result.isRisky());
        assertFalse(result.isUnknown());
        assertTrue(result.isFreeEmail());
        assertFalse(result.isRole());
        assertFalse(result.isDisposable());
    }

    @Test
    void validateReturnsInvalidResult() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(okJson("{" +
                        "\"state\":\"invalid\"," +
                        "\"sub_state\":\"failed_no_mailbox\"," +
                        "\"suggestion\":null," +
                        "\"free_email\":false," +
                        "\"role\":false," +
                        "\"disposable\":false" +
                        "}")));

        ValidationResult result = client.validate("bad@example.com");

        assertEquals("invalid", result.getState());
        assertEquals("failed_no_mailbox", result.getSubState());
        assertTrue(result.isInvalid());
        assertFalse(result.isValid());
    }

    @Test
    void validateReturnsRiskyResult() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(okJson("{" +
                        "\"state\":\"risky\"," +
                        "\"sub_state\":\"disposable_address\"," +
                        "\"suggestion\":null," +
                        "\"free_email\":false," +
                        "\"role\":false," +
                        "\"disposable\":true" +
                        "}")));

        ValidationResult result = client.validate("user@tempmail.com");

        assertEquals("risky", result.getState());
        assertTrue(result.isRisky());
        assertTrue(result.isDisposable());
        assertFalse(result.isValid());
        assertTrue(result.isValid(true));
    }

    @Test
    void validateReturnsUnknownResult() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(okJson("{" +
                        "\"state\":\"unknown\"," +
                        "\"sub_state\":\"unknown\"," +
                        "\"suggestion\":null," +
                        "\"free_email\":false," +
                        "\"role\":false," +
                        "\"disposable\":false" +
                        "}")));

        ValidationResult result = client.validate("user@mystery.com");

        assertEquals("unknown", result.getState());
        assertTrue(result.isUnknown());
        assertFalse(result.isValid());
        assertFalse(result.isValid(true));
    }

    @Test
    void validateSendsCorrectRequestBody() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .withRequestBody(equalToJson("{\"email\":\"test@example.com\"}"))
                .willReturn(okJson("{\"state\":\"valid\",\"sub_state\":\"ok\"," +
                        "\"free_email\":false,\"role\":false,\"disposable\":false}")));

        client.validate("test@example.com");

        wireMock.verify(postRequestedFor(urlEqualTo("/api/v1/verify"))
                .withRequestBody(equalToJson("{\"email\":\"test@example.com\"}")));
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
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(okJson("{" +
                        "\"state\":\"invalid\"," +
                        "\"sub_state\":\"failed_syntax_check\"," +
                        "\"suggestion\":\"user@gmail.com\"," +
                        "\"free_email\":false," +
                        "\"role\":false," +
                        "\"disposable\":false" +
                        "}")));

        ValidationResult result = client.validate("user@gmial.com");

        assertEquals("user@gmail.com", result.getSuggestion());
    }

    // --- formValidate() Tests ---

    @Test
    void formValidateUsesFormApiKey() {
        Truelist formClient = Truelist.builder("server-key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(0)
                .formApiKey("form-api-key")
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/form_verify"))
                .withHeader("Authorization", equalTo("Bearer form-api-key"))
                .willReturn(okJson("{\"state\":\"valid\",\"sub_state\":\"ok\"," +
                        "\"free_email\":false,\"role\":false,\"disposable\":false}")));

        ValidationResult result = formClient.formValidate("user@example.com");

        assertEquals("valid", result.getState());
        wireMock.verify(postRequestedFor(urlEqualTo("/api/v1/form_verify"))
                .withHeader("Authorization", equalTo("Bearer form-api-key")));
    }

    @Test
    void formValidateThrowsWithoutFormApiKey() {
        assertThrows(IllegalStateException.class, () ->
                client.formValidate("user@example.com"));
    }

    @Test
    void formValidateRejectsNullEmail() {
        Truelist formClient = Truelist.builder("key")
                .formApiKey("form-key")
                .build();
        assertThrows(IllegalArgumentException.class, () ->
                formClient.formValidate(null));
    }

    // --- getAccount() Tests ---

    @Test
    void getAccountReturnsAccountInfo() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/account"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .willReturn(okJson("{" +
                        "\"email\":\"user@company.com\"," +
                        "\"plan\":\"pro\"," +
                        "\"credits\":9542" +
                        "}")));

        AccountInfo account = client.getAccount();

        assertEquals("user@company.com", account.getEmail());
        assertEquals("pro", account.getPlan());
        assertEquals(9542, account.getCredits());
    }

    // --- Error Handling Tests ---

    @Test
    void authErrorAlwaysThrowsOnValidate() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                client.validate("user@example.com"));

        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void authErrorAlwaysThrowsOnGetAccount() {
        wireMock.stubFor(get(urlEqualTo("/api/v1/account"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        assertThrows(AuthenticationException.class, () -> client.getAccount());
    }

    @Test
    void authErrorAlwaysThrowsOnFormValidate() {
        Truelist formClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .maxRetries(0)
                .formApiKey("bad-form-key")
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/form_verify"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        assertThrows(AuthenticationException.class, () ->
                formClient.formValidate("user@example.com"));
    }

    @Test
    void rateLimitThrows429Exception() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(aResponse().withStatus(429).withBody("Too Many Requests")));

        RateLimitException ex = assertThrows(RateLimitException.class, () ->
                client.validate("user@example.com"));

        assertEquals(429, ex.getStatusCode());
    }

    @Test
    void serverErrorThrowsApiException() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(serverError().withBody("Internal Server Error")));

        ApiException ex = assertThrows(ApiException.class, () ->
                client.validate("user@example.com"));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void unexpectedStatusThrowsApiException() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
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

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(serverError().withBody("Error"))
                .willSetStateTo("first-retry"));

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .inScenario("retry")
                .whenScenarioStateIs("first-retry")
                .willReturn(serverError().withBody("Error"))
                .willSetStateTo("second-retry"));

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .inScenario("retry")
                .whenScenarioStateIs("second-retry")
                .willReturn(okJson("{\"state\":\"valid\",\"sub_state\":\"ok\"," +
                        "\"free_email\":false,\"role\":false,\"disposable\":false}")));

        ValidationResult result = retryClient.validate("user@example.com");

        assertEquals("valid", result.getState());
        wireMock.verify(3, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }

    @Test
    void doesNotRetryAuthErrors() {
        Truelist retryClient = Truelist.builder("bad-key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(unauthorized().withBody("Unauthorized")));

        assertThrows(AuthenticationException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(1, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }

    @Test
    void retriesOnRateLimitError() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .inScenario("rate-limit-retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(429).withBody("Rate limited"))
                .willSetStateTo("first-retry"));

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .inScenario("rate-limit-retry")
                .whenScenarioStateIs("first-retry")
                .willReturn(okJson("{\"state\":\"valid\",\"sub_state\":\"ok\"," +
                        "\"free_email\":false,\"role\":false,\"disposable\":false}")));

        ValidationResult result = retryClient.validate("user@example.com");

        assertEquals("valid", result.getState());
        wireMock.verify(2, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }

    @Test
    void exhaustsRetriesOnPersistentRateLimitError() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(1)
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(aResponse().withStatus(429).withBody("Rate limited")));

        assertThrows(RateLimitException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(2, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }

    @Test
    void doesNotRetryClientErrors() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(2)
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(aResponse().withStatus(403).withBody("Forbidden")));

        assertThrows(ApiException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(1, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }

    @Test
    void exhaustsRetriesOnPersistentServerError() {
        Truelist retryClient = Truelist.builder("key")
                .baseUrl("http://localhost:" + wireMock.port())
                .timeout(Duration.ofSeconds(5))
                .maxRetries(1)
                .build();

        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(serverError().withBody("Error")));

        assertThrows(ApiException.class, () ->
                retryClient.validate("user@example.com"));

        wireMock.verify(2, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }

    @Test
    void zeroRetriesMeansOneAttempt() {
        wireMock.stubFor(post(urlEqualTo("/api/v1/verify"))
                .willReturn(serverError().withBody("Error")));

        assertThrows(ApiException.class, () -> client.validate("user@example.com"));

        wireMock.verify(1, postRequestedFor(urlEqualTo("/api/v1/verify")));
    }
}
