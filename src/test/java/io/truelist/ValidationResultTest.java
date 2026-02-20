package io.truelist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void validEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "valid", "ok", null, true, false, false);

        assertTrue(result.isValid());
        assertTrue(result.isValid(false));
        assertTrue(result.isValid(true));
        assertFalse(result.isInvalid());
        assertFalse(result.isRisky());
        assertFalse(result.isUnknown());
    }

    @Test
    void invalidEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "invalid", "failed_no_mailbox", null, false, false, false);

        assertFalse(result.isValid());
        assertFalse(result.isValid(true));
        assertTrue(result.isInvalid());
        assertFalse(result.isRisky());
        assertFalse(result.isUnknown());
    }

    @Test
    void riskyEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "risky", "disposable_address", null, false, false, true);

        assertFalse(result.isValid());
        assertFalse(result.isValid(false));
        assertTrue(result.isValid(true));
        assertFalse(result.isInvalid());
        assertTrue(result.isRisky());
        assertFalse(result.isUnknown());
        assertTrue(result.isDisposable());
    }

    @Test
    void unknownEmailPredicates() {
        ValidationResult result = new ValidationResult(
                "unknown", "unknown", null, false, false, false);

        assertFalse(result.isValid());
        assertFalse(result.isValid(true));
        assertFalse(result.isInvalid());
        assertFalse(result.isRisky());
        assertTrue(result.isUnknown());
    }

    @Test
    void freeEmailFlag() {
        ValidationResult result = new ValidationResult(
                "valid", "ok", null, true, false, false);
        assertTrue(result.isFreeEmail());

        ValidationResult nonFree = new ValidationResult(
                "valid", "ok", null, false, false, false);
        assertFalse(nonFree.isFreeEmail());
    }

    @Test
    void roleFlag() {
        ValidationResult result = new ValidationResult(
                "risky", "role_address", null, false, true, false);
        assertTrue(result.isRole());

        ValidationResult nonRole = new ValidationResult(
                "valid", "ok", null, false, false, false);
        assertFalse(nonRole.isRole());
    }

    @Test
    void disposableFlag() {
        ValidationResult result = new ValidationResult(
                "risky", "disposable_address", null, false, false, true);
        assertTrue(result.isDisposable());

        ValidationResult nonDisposable = new ValidationResult(
                "valid", "ok", null, false, false, false);
        assertFalse(nonDisposable.isDisposable());
    }

    @Test
    void suggestion() {
        ValidationResult withSuggestion = new ValidationResult(
                "invalid", "failed_syntax_check", "user@gmail.com",
                false, false, false);
        assertEquals("user@gmail.com", withSuggestion.getSuggestion());

        ValidationResult withoutSuggestion = new ValidationResult(
                "valid", "ok", null, false, false, false);
        assertNull(withoutSuggestion.getSuggestion());
    }

    @Test
    void gettersReturnCorrectValues() {
        ValidationResult result = new ValidationResult(
                "valid", "ok", "suggestion@test.com", true, true, true);

        assertEquals("valid", result.getState());
        assertEquals("ok", result.getSubState());
        assertEquals("suggestion@test.com", result.getSuggestion());
        assertTrue(result.isFreeEmail());
        assertTrue(result.isRole());
        assertTrue(result.isDisposable());
    }

    @Test
    void toStringContainsAllFields() {
        ValidationResult result = new ValidationResult(
                "valid", "ok", null, true, false, false);

        String str = result.toString();
        assertTrue(str.contains("valid"));
        assertTrue(str.contains("ok"));
        assertTrue(str.contains("freeEmail=true"));
        assertTrue(str.contains("role=false"));
        assertTrue(str.contains("disposable=false"));
    }
}
