package apap.ti._5.accommodation_2306211231_be.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenBlacklistTest {

    private JwtTokenBlacklist blacklist;

    @BeforeEach
    void setUp() {
        blacklist = new JwtTokenBlacklist();
    }

    @Test
    void revoke_nullToken_doesNotThrow() {
        assertDoesNotThrow(() -> blacklist.revoke(null, System.currentTimeMillis() + 10000));
    }

    @Test
    void revoke_blankToken_doesNotThrow() {
        assertDoesNotThrow(() -> blacklist.revoke("   ", System.currentTimeMillis() + 10000));
    }

    @Test
    void revoke_validToken_addsToBlacklist() {
        String token = "valid-token-123";
        long expiry = System.currentTimeMillis() + 60000;
        blacklist.revoke(token, expiry);
        assertTrue(blacklist.isRevoked(token));
    }

    @Test
    void isRevoked_nullToken_returnsFalse() {
        assertFalse(blacklist.isRevoked(null));
    }

    @Test
    void isRevoked_blankToken_returnsFalse() {
        assertFalse(blacklist.isRevoked("   "));
    }

    @Test
    void isRevoked_unknownToken_returnsFalse() {
        assertFalse(blacklist.isRevoked("unknown-token"));
    }

    @Test
    void isRevoked_expiredToken_returnsFalseAndRemovesFromMap() {
        String token = "expired-token";
        long pastExpiry = System.currentTimeMillis() - 1000;
        blacklist.revoke(token, pastExpiry);
        // Token expired so should return false and be removed
        assertFalse(blacklist.isRevoked(token));
        // Check again - should still be false
        assertFalse(blacklist.isRevoked(token));
    }

    @Test
    void isRevoked_activeToken_returnsTrue() {
        String token = "active-token";
        long futureExpiry = System.currentTimeMillis() + 60000;
        blacklist.revoke(token, futureExpiry);
        assertTrue(blacklist.isRevoked(token));
    }

    @Test
    void multipleTokens_trackedIndependently() {
        String token1 = "token-1";
        String token2 = "token-2";
        long futureExpiry = System.currentTimeMillis() + 60000;
        
        blacklist.revoke(token1, futureExpiry);
        assertTrue(blacklist.isRevoked(token1));
        assertFalse(blacklist.isRevoked(token2));
        
        blacklist.revoke(token2, futureExpiry);
        assertTrue(blacklist.isRevoked(token1));
        assertTrue(blacklist.isRevoked(token2));
    }

    @Test
    void revoke_emptyString_doesNotThrow() {
        assertDoesNotThrow(() -> blacklist.revoke("", System.currentTimeMillis() + 10000));
        assertFalse(blacklist.isRevoked(""));
    }
}
