package io.truelist;

import com.google.gson.annotations.SerializedName;

/**
 * Represents account information from the Truelist API.
 */
public class AccountInfo {

    private final String email;
    private final String name;
    private final String uuid;

    @SerializedName("time_zone")
    private final String timeZone;

    @SerializedName("is_admin_role")
    private final boolean adminRole;

    private final Account account;

    /**
     * Nested account details.
     */
    public static class Account {
        private final String name;

        @SerializedName("payment_plan")
        private final String paymentPlan;

        public Account(String name, String paymentPlan) {
            this.name = name;
            this.paymentPlan = paymentPlan;
        }

        public String getName() {
            return name;
        }

        public String getPaymentPlan() {
            return paymentPlan;
        }

        @Override
        public String toString() {
            return "Account{" +
                    "name='" + name + '\'' +
                    ", paymentPlan='" + paymentPlan + '\'' +
                    '}';
        }
    }

    /**
     * Creates a new AccountInfo.
     *
     * @param email     the account email address
     * @param name      the user name
     * @param uuid      the user UUID
     * @param timeZone  the user's time zone
     * @param adminRole whether the user has admin role
     * @param account   the nested account details
     */
    public AccountInfo(String email, String name, String uuid,
                       String timeZone, boolean adminRole, Account account) {
        this.email = email;
        this.name = name;
        this.uuid = uuid;
        this.timeZone = timeZone;
        this.adminRole = adminRole;
        this.account = account;
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
     * Returns the user name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the user UUID.
     *
     * @return the UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the user's time zone.
     *
     * @return the time zone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Returns whether the user has admin role.
     *
     * @return true if admin
     */
    public boolean isAdminRole() {
        return adminRole;
    }

    /**
     * Returns the nested account details.
     *
     * @return the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Returns the payment plan from the nested account.
     *
     * @return the plan name, or null if account is null
     */
    public String getPlan() {
        return account != null ? account.getPaymentPlan() : null;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", adminRole=" + adminRole +
                ", account=" + account +
                '}';
    }
}
