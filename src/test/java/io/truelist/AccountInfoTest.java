package io.truelist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountInfoTest {

    @Test
    void gettersReturnCorrectValues() {
        AccountInfo.Account account = new AccountInfo.Account("Company Inc", "pro");
        AccountInfo info = new AccountInfo(
                "team@company.com", "Team Lead",
                "a3828d19-1234-5678-9abc-def012345678",
                "America/New_York", true, account);

        assertEquals("team@company.com", info.getEmail());
        assertEquals("Team Lead", info.getName());
        assertEquals("a3828d19-1234-5678-9abc-def012345678", info.getUuid());
        assertEquals("America/New_York", info.getTimeZone());
        assertTrue(info.isAdminRole());
        assertNotNull(info.getAccount());
        assertEquals("Company Inc", info.getAccount().getName());
        assertEquals("pro", info.getAccount().getPaymentPlan());
        assertEquals("pro", info.getPlan());
    }

    @Test
    void toStringContainsAllFields() {
        AccountInfo.Account account = new AccountInfo.Account("Company", "starter");
        AccountInfo info = new AccountInfo(
                "user@company.com", "User",
                "uuid-123", "UTC", false, account);

        String str = info.toString();
        assertTrue(str.contains("user@company.com"));
        assertTrue(str.contains("User"));
        assertTrue(str.contains("uuid-123"));
        assertTrue(str.contains("UTC"));
    }

    @Test
    void getPlanReturnsNullWithoutAccount() {
        AccountInfo info = new AccountInfo(
                "user@example.com", "User",
                "uuid", "UTC", false, null);
        assertNull(info.getPlan());
    }

    @Test
    void differentPlans() {
        AccountInfo.Account free = new AccountInfo.Account("Co", "free");
        AccountInfo.Account pro = new AccountInfo.Account("Co", "pro");
        AccountInfo.Account enterprise = new AccountInfo.Account("Co", "enterprise");

        assertEquals("free", free.getPaymentPlan());
        assertEquals("pro", pro.getPaymentPlan());
        assertEquals("enterprise", enterprise.getPaymentPlan());
    }
}
