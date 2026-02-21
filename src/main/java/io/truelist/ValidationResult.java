package io.truelist;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the result of an email validation request.
 */
public class ValidationResult {

    @SerializedName("address")
    private final String email;

    private final String domain;

    private final String canonical;

    @SerializedName("mx_record")
    private final String mxRecord;

    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    @SerializedName("email_state")
    private final String state;

    @SerializedName("email_sub_state")
    private final String subState;

    @SerializedName("verified_at")
    private final String verifiedAt;

    @SerializedName("did_you_mean")
    private final String suggestion;

    /**
     * Creates a new ValidationResult.
     *
     * @param email      the email address
     * @param domain     the email domain
     * @param canonical  the canonical local part
     * @param mxRecord   the MX record, or null
     * @param firstName  the first name, or null
     * @param lastName   the last name, or null
     * @param state      the validation state (ok, email_invalid, accept_all, unknown)
     * @param subState   the validation sub-state
     * @param verifiedAt the verification timestamp
     * @param suggestion a suggested correction, or null
     */
    public ValidationResult(String email, String domain, String canonical,
                            String mxRecord, String firstName, String lastName,
                            String state, String subState, String verifiedAt,
                            String suggestion) {
        this.email = email;
        this.domain = domain;
        this.canonical = canonical;
        this.mxRecord = mxRecord;
        this.firstName = firstName;
        this.lastName = lastName;
        this.state = state;
        this.subState = subState;
        this.verifiedAt = verifiedAt;
        this.suggestion = suggestion;
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the email domain.
     *
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the canonical local part of the email.
     *
     * @return the canonical part
     */
    public String getCanonical() {
        return canonical;
    }

    /**
     * Returns the MX record for the domain, or null.
     *
     * @return the MX record
     */
    public String getMxRecord() {
        return mxRecord;
    }

    /**
     * Returns the first name associated with the email, or null.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name associated with the email, or null.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the validation state.
     *
     * @return one of "ok", "email_invalid", "accept_all", or "unknown"
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the validation sub-state providing more detail about the result.
     *
     * @return the sub-state (e.g., "email_ok", "is_disposable", "is_role")
     */
    public String getSubState() {
        return subState;
    }

    /**
     * Returns the verification timestamp.
     *
     * @return the verified_at timestamp string
     */
    public String getVerifiedAt() {
        return verifiedAt;
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
     * @return true if the state is "ok"
     */
    public boolean isValid() {
        return "ok".equals(state);
    }

    /**
     * Returns whether the email is invalid.
     *
     * @return true if the state is "email_invalid"
     */
    public boolean isInvalid() {
        return "email_invalid".equals(state);
    }

    /**
     * Returns whether the domain accepts all addresses.
     *
     * @return true if the state is "accept_all"
     */
    public boolean isAcceptAll() {
        return "accept_all".equals(state);
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
     * Returns whether the email is a disposable/temporary address.
     *
     * @return true if the sub-state is "is_disposable"
     */
    public boolean isDisposable() {
        return "is_disposable".equals(subState);
    }

    /**
     * Returns whether the email is a role address (e.g., admin@, support@).
     *
     * @return true if the sub-state is "is_role"
     */
    public boolean isRole() {
        return "is_role".equals(subState);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "email='" + email + '\'' +
                ", domain='" + domain + '\'' +
                ", canonical='" + canonical + '\'' +
                ", mxRecord='" + mxRecord + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", state='" + state + '\'' +
                ", subState='" + subState + '\'' +
                ", verifiedAt='" + verifiedAt + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}
