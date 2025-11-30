package apap.ti._5.accommodation_2306211231_be.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceTest {

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService();
    }

    @Test
    void createRefreshToken_withMetadata_returnsToken() {
        String token = service.createRefreshToken("user1", "192.168.1.1", "Mozilla/5.0");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void createRefreshToken_withoutMetadata_returnsToken() {
        String token = service.createRefreshToken("user1");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void createRefreshToken_nullIpAndUa_handlesGracefully() {
        String token = service.createRefreshToken("user1", null, null);
        assertNotNull(token);
        assertTrue(service.validateRefreshToken(token, "user1"));
    }

    @Test
    void validateRefreshToken_nullToken_returnsFalse() {
        assertFalse(service.validateRefreshToken(null, "user1"));
    }

    @Test
    void validateRefreshToken_blankToken_returnsFalse() {
        assertFalse(service.validateRefreshToken("   ", "user1"));
    }

    @Test
    void validateRefreshToken_unknownToken_returnsFalse() {
        assertFalse(service.validateRefreshToken("unknown-token", "user1"));
    }

    @Test
    void validateRefreshToken_wrongUsername_returnsFalse() {
        String token = service.createRefreshToken("user1");
        assertFalse(service.validateRefreshToken(token, "user2"));
    }

    @Test
    void validateRefreshToken_validToken_returnsTrue() {
        String token = service.createRefreshToken("user1");
        assertTrue(service.validateRefreshToken(token, "user1"));
    }

    @Test
    void rotateRefreshToken_validToken_returnsNewToken() {
        String oldToken = service.createRefreshToken("user1", "192.168.1.1", "Chrome");
        String newToken = service.rotateRefreshToken(oldToken, "user1", "192.168.1.2", "Firefox");
        
        assertNotNull(newToken);
        assertNotEquals(oldToken, newToken);
        assertFalse(service.validateRefreshToken(oldToken, "user1"));
        assertTrue(service.validateRefreshToken(newToken, "user1"));
    }

    @Test
    void rotateRefreshToken_invalidToken_returnsNull() {
        String result = service.rotateRefreshToken("invalid", "user1", "ip", "ua");
        assertNull(result);
    }

    @Test
    void rotateRefreshToken_wrongUsername_returnsNull() {
        String token = service.createRefreshToken("user1");
        String result = service.rotateRefreshToken(token, "user2", "ip", "ua");
        assertNull(result);
    }

    @Test
    void revokeRefreshToken_nullToken_noException() {
        assertDoesNotThrow(() -> service.revokeRefreshToken(null));
    }

    @Test
    void revokeRefreshToken_validToken_removesFromStore() {
        String token = service.createRefreshToken("user1");
        assertTrue(service.validateRefreshToken(token, "user1"));
        
        service.revokeRefreshToken(token);
        assertFalse(service.validateRefreshToken(token, "user1"));
    }

    @Test
    void revokeRefreshToken_unknownToken_noException() {
        assertDoesNotThrow(() -> service.revokeRefreshToken("unknown-token"));
    }

    @Test
    void revokeAllTokensForUser_nullUsername_noException() {
        assertDoesNotThrow(() -> service.revokeAllTokensForUser(null));
    }

    @Test
    void revokeAllTokensForUser_validUser_revokesAllTokens() {
        String token1 = service.createRefreshToken("user1");
        String token2 = service.createRefreshToken("user1");
        String token3 = service.createRefreshToken("user2");
        
        assertTrue(service.validateRefreshToken(token1, "user1"));
        assertTrue(service.validateRefreshToken(token2, "user1"));
        assertTrue(service.validateRefreshToken(token3, "user2"));
        
        service.revokeAllTokensForUser("user1");
        
        assertFalse(service.validateRefreshToken(token1, "user1"));
        assertFalse(service.validateRefreshToken(token2, "user1"));
        assertTrue(service.validateRefreshToken(token3, "user2"));
    }

    @Test
    void revokeAllTokensForUser_unknownUser_noException() {
        assertDoesNotThrow(() -> service.revokeAllTokensForUser("unknownUser"));
    }

    @Test
    void multipleUsersIndependent() {
        String tokenA = service.createRefreshToken("userA");
        String tokenB = service.createRefreshToken("userB");
        
        assertTrue(service.validateRefreshToken(tokenA, "userA"));
        assertTrue(service.validateRefreshToken(tokenB, "userB"));
        assertFalse(service.validateRefreshToken(tokenA, "userB"));
        assertFalse(service.validateRefreshToken(tokenB, "userA"));
    }
}
