package apap.ti._5.accommodation_2306211231_be.security.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenBlacklist {

    // token -> expirationTimeMillis
    private final ConcurrentHashMap<String, Long> revoked = new ConcurrentHashMap<>();

    public void revoke(String token, long expirationTimeMillis) {
        if (token == null || token.isBlank()) return;
        revoked.put(token, expirationTimeMillis);
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) return false;
        Long exp = revoked.get(token);
        if (exp == null) return false;
        if (System.currentTimeMillis() > exp) {
            // token already expired naturally, remove from map
            revoked.remove(token);
            return false;
        }
        return true;
    }
}
