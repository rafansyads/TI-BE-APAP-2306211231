package apap.ti._5.accommodation_2306211231_be.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtUtilsExtraTest {

    @Test
    void validate_token_error_branches_and_helpers() throws Exception {
        JwtUtils ju = new JwtUtils();

        Field fSecret = JwtUtils.class.getDeclaredField("jwtSecret");
        fSecret.setAccessible(true);
        fSecret.set(ju, "01234567890123456789012345678901");

        Field fExp = JwtUtils.class.getDeclaredField("jwtExpirationMs");
        fExp.setAccessible(true);
        // expired token
        fExp.setInt(ju, -1000);

        UUID id = UUID.randomUUID();
        String expired = ju.generateJwtToken(id, "eve", "e@e.com", "Eve", List.of("CUSTOMER"));
        assertNotNull(expired);
        assertFalse(ju.validateJwtToken(expired));

        // malformed token should be rejected
        assertFalse(ju.validateJwtToken("not.a.jwt"));

        // tampered signature should be rejected
        // create a normal token with positive exp then tamper
        fExp.setInt(ju, 3600000);
        String token = ju.generateJwtToken(id, "sam", "s@e.com", "Sam", List.of("CUSTOMER"));
        assertTrue(ju.validateJwtToken(token));
        String tampered = token + "x";
        assertFalse(ju.validateJwtToken(tampered));

        // getExpirationFromJwtToken returns a Date
        Date exp = ju.getExpirationFromJwtToken(token);
        assertNotNull(exp);

        // set and read jwtExpirationMs via reflection
        Field fExpCheck = JwtUtils.class.getDeclaredField("jwtExpirationMs");
        fExpCheck.setAccessible(true);
        fExpCheck.setInt(ju, 12345);
        assertEquals(12345, ju.getJwtExpirationMs());

        // getCurrentUsername reads from SecurityContextHolder
        var auth = new UsernamePasswordAuthenticationToken("zack", "x", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals("zack", ju.getCurrentUsername());
        SecurityContextHolder.clearContext();
    }
}
