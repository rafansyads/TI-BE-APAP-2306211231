package apap.ti._5.accommodation_2306211231_be.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist;

class JwtUtilsTest {

    @Test
    void generate_and_parse_token_and_blacklist_behavior() throws Exception {
        JwtUtils ju = new JwtUtils();

        // set secret and expiration via reflection
        Field fSecret = JwtUtils.class.getDeclaredField("jwtSecret");
        fSecret.setAccessible(true);
        // 32-byte key
        fSecret.set(ju, "01234567890123456789012345678901");

        Field fExp = JwtUtils.class.getDeclaredField("jwtExpirationMs");
        fExp.setAccessible(true);
        fExp.setInt(ju, 3600000);

        // no blacklist -> token valid
        UUID id = UUID.randomUUID();
        String token = ju.generateJwtToken(id, "alice", "a@e.com", "Alice", List.of("CUSTOMER"));
        assertNotNull(token);

        assertEquals("alice", ju.getUserNameFromJwtToken(token));
        assertEquals(id.toString(), ju.getIdFromJwtToken(token));
        assertEquals("a@e.com", ju.getEmailFromJwtToken(token));
        assertEquals("Alice", ju.getNameFromJwtToken(token));
        assertEquals(List.of("CUSTOMER"), ju.getRolesFromJwtToken(token));
        assertNotNull(ju.getExpirationFromJwtToken(token));
        assertTrue(ju.validateJwtToken(token));

        // attach a mocked blacklist that revokes the token
        JwtTokenBlacklist blacklist = mock(JwtTokenBlacklist.class);
        when(blacklist.isRevoked(token)).thenReturn(true);
        Field fBL = JwtUtils.class.getDeclaredField("jwtTokenBlacklist");
        fBL.setAccessible(true);
        fBL.set(ju, blacklist);

        assertFalse(ju.validateJwtToken(token));
    }
}
