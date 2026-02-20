package io.truelist;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the result of an email validation request.
 *
 * <p>The result contains the validation state, sub-state, and various flags
 * indicating properties of the email address.</p>
 */
public class ValidationResult {

    private final String state;

    @SerializedName("sub_state")
    private final String subState;

    private final String suggestion;

    @SerializedName("free_email")
    private final boolean freeEmail;

    private final boolean role;

    private final boolean disposable;

    /**
     * Creates a new ValidationResult.
     *
     * @param state      the validation state (valid, invalid, risky, unknown)
     * @param subState   the validation sub-state
     * @param suggestion a suggested correction, or null
     * @param freeEmail  whether the email is from a free provider
     * @param role       whether the email is a role address
     * @param disposable whether the email is a disposable address
     */
    public ValidationResult(String state, String subState, String suggestion,
                            boolean freeEmail, boolean role, boolean disposable) {
        this.state = state;
        this.subState = subState;
        this.suggestion = suggestion;
        this.freeEmail = freeEmail;
        this.role = role;
        this.disposable = disposable;
    }

    /**
     * Returns the validation state.
     *
     * @return one of "valid", "invalid", "risky", or "unknown"
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the validation sub-state providing more detail about the result.
     *
     * @return the sub-state (e.g., "ok", "disposable_address", "role_address")
     */
    public String getSubState() {
        return subState;
    }

    /**
     * Returns a suggested correction for the email address, if any.
     *
     * @return the suggestion, or null if no suggestion
     */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * Returns whether the email is valid.
     *
     * @return true if the state is "valid"
     */
    public boolean isValid() {
        return "valid".equals(state);
    }

    /**
     * Returns whether the email is valid, optionally treating risky emails as valid.
     *
     * @param allowRisky if true, both "valid" and "risky" states are considered valid
     * @return true if the email passes validation
     */
    public boolean isValid(boolean allowRisky) {
        if (allowRisky) {
            return "valid".equals(state) || "risky".equals(state);
        }
        return "valid".equals(state);
    }

    /**
     * Returns whether the email is invalid.
     *
     * @return true if the state is "invalid"
     */
    public boolean isInvalid() {
        return "invalid".equals(state);
    }

    /**
     * Returns whether the email is risky.
     *
     * @return true if the state is "risky"
     */
    public boolean isRisky() {
        return "risky".equals(state);
    }

    /**
     * Returns whether the validation result is unknown.
     *
     * @return true if the state is "unknown"
     */
    public boolean isUnknown() {
        return "unknown".equals(state);
    }

    /**
     * Returns whether the email is from a free email provider.
     *
     * @return true if the email is free
     */
    public boolean isFreeEmail() {
        return freeEmail;
    }

    /**
     * Returns whether the email is a role address (e.g., admin@, support@).
     *
     * @return true if the email is a role address
     */
    public boolean isRole() {
        return role;
    }

    /**
     * Returns whether the email is a disposable/temporary address.
     *
     * @return true if the email is disposable
     */
    public boolean isDisposable() {
        return disposable;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "state='" + state + '\'' +
                ", subState='" + subState + '\'' +
                ", suggestion='" + suggestion + '\'' +
                ", freeEmail=" + freeEmail +
                ", role=" + role +
                ", disposable=" + disposable +
                '}';
    }
}
