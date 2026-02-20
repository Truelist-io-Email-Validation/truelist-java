package io.truelist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountInfoTest {

    @Test
    void gettersReturnCorrectValues() {
        AccountInfo account = new AccountInfo("user@company.com", "pro", 9542);

        assertEquals("user@company.com", account.getEmail());
        assertEquals("pro", account.getPlan());
        assertEquals(9542, account.getCredits());
    }

    @Test
    void toStringContainsAllFields() {
        AccountInfo account = new AccountInfo("user@company.com", "starter", 100);

        String str = account.toString();
        assertTrue(str.contains("user@company.com"));
        assertTrue(str.contains("starter"));
        assertTrue(str.contains("100"));
    }

    @Test
    void zeroCredits() {
        AccountInfo account = new AccountInfo("user@example.com", "free", 0);
        assertEquals(0, account.getCredits());
    }

    @Test
    void differentPlans() {
        AccountInfo free = new AccountInfo("a@b.com", "free", 10);
        AccountInfo pro = new AccountInfo("a@b.com", "pro", 5000);
        AccountInfo enterprise = new AccountInfo("a@b.com", "enterprise", 100000);

        assertEquals("free", free.getPlan());
        assertEquals("pro", pro.getPlan());
        assertEquals("enterprise", enterprise.getPlan());
    }
}
