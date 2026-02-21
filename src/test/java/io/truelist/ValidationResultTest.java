package io.truelist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void validEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "user@example.com", "example.com", "user",
                null, null, null,
                "ok", "email_ok", "2026-02-21T10:00:00.000Z", null);

        assertTrue(result.isValid());
        assertFalse(result.isInvalid());
        assertFalse(result.isAcceptAll());
        assertFalse(result.isUnknown());
    }

    @Test
    void invalidEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "bad@example.com", "example.com", "bad",
                null, null, null,
                "email_invalid", "failed_smtp_check", "2026-02-21T10:00:00.000Z", null);

        assertFalse(result.isValid());
        assertTrue(result.isInvalid());
        assertFalse(result.isAcceptAll());
        assertFalse(result.isUnknown());
    }

    @Test
    void acceptAllPredicates() {
        ValidationResult result = new ValidationResult(
                "user@catchall.com", "catchall.com", "user",
                null, null, null,
                "accept_all", "email_ok", "2026-02-21T10:00:00.000Z", null);

        assertFalse(result.isValid());
        assertFalse(result.isInvalid());
        assertTrue(result.isAcceptAll());
        assertFalse(result.isUnknown());
    }

    @Test
    void unknownEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "user@mystery.com", "mystery.com", "user",
                null, null, null,
                "unknown", "unknown_error", "2026-02-21T10:00:00.000Z", null);

        assertFalse(result.isValid());
        assertFalse(result.isInvalid());
        assertFalse(result.isAcceptAll());
        assertTrue(result.isUnknown());
    }

    @Test
    void disposableSubState() {
        ValidationResult result = new ValidationResult(
                "user@tempmail.com", "tempmail.com", "user",
                null, null, null,
                "ok", "is_disposable", "2026-02-21T10:00:00.000Z", null);

        assertTrue(result.isDisposable());
        assertFalse(result.isRole());
    }

    @Test
    void roleSubState() {
        ValidationResult result = new ValidationResult(
                "admin@company.com", "company.com", "admin",
                null, null, null,
                "ok", "is_role", "2026-02-21T10:00:00.000Z", null);

        assertTrue(result.isRole());
        assertFalse(result.isDisposable());
    }

    @Test
    void suggestion() {
        ValidationResult withSuggestion = new ValidationResult(
                "user@gmial.com", "gmial.com", "user",
                null, null, null,
                "email_invalid", "unknown_error", "2026-02-21T10:00:00.000Z",
                "user@gmail.com");
        assertEquals("user@gmail.com", withSuggestion.getSuggestion());

        ValidationResult withoutSuggestion = new ValidationResult(
                "user@example.com", "example.com", "user",
                null, null, null,
                "ok", "email_ok", "2026-02-21T10:00:00.000Z", null);
        assertNull(withoutSuggestion.getSuggestion());
    }

    @Test
    void gettersReturnCorrectValues() {
        ValidationResult result = new ValidationResult(
                "user@example.com", "example.com", "user",
                "mx.example.com", "John", "Doe",
                "ok", "email_ok", "2026-02-21T10:00:00.000Z", null);

        assertEquals("user@example.com", result.getEmail());
        assertEquals("example.com", result.getDomain());
        assertEquals("user", result.getCanonical());
        assertEquals("mx.example.com", result.getMxRecord());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("ok", result.getState());
        assertEquals("email_ok", result.getSubState());
        assertEquals("2026-02-21T10:00:00.000Z", result.getVerifiedAt());
        assertNull(result.getSuggestion());
    }

    @Test
    void toStringContainsAllFields() {
        ValidationResult result = new ValidationResult(
                "user@example.com", "example.com", "user",
                null, null, null,
                "ok", "email_ok", "2026-02-21T10:00:00.000Z", null);

        String str = result.toString();
        assertTrue(str.contains("user@example.com"));
        assertTrue(str.contains("example.com"));
        assertTrue(str.contains("ok"));
        assertTrue(str.contains("email_ok"));
    }
}
