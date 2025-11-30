package apap.ti._5.accommodation_2306211231_be.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TestSecurityUtils {
    private TestSecurityUtils() {}

    public static Authentication authWithRoles(String username, String... roles) {
        var auths = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(username, null, auths);
    }
}
