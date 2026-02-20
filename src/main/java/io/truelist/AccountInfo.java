package io.truelist;

/**
 * Represents account information from the Truelist API.
 */
public class AccountInfo {

    private final String email;
    private final String plan;
    private final int credits;

    /**
     * Creates a new AccountInfo.
     *
     * @param email   the account email address
     * @param plan    the account plan name
     * @param credits the number of remaining credits
     */
    public AccountInfo(String email, String plan, int credits) {
        this.email = email;
        this.plan = plan;
        this.credits = credits;
    }

    /**
     * Returns the account email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the account plan name.
     *
     * @return the plan name
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Returns the number of remaining validation credits.
     *
     * @return the credit count
     */
    public int getCredits() {
        return credits;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "email='" + email + '\'' +
                ", plan='" + plan + '\'' +
                ", credits=" + credits +
                '}';
    }
}
