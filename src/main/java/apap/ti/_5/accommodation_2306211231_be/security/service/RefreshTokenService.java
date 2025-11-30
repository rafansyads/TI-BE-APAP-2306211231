package apap.ti._5.accommodation_2306211231_be.security.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory refresh token store with metadata. Thread-safe using concurrent collections.
 * Stores: username, issuedAt, ip, ua, expiresAt. Also keeps per-user set for revocation.
 */
@Service
public class RefreshTokenService {

    private static class Meta {
        final String username;
        final long issuedAt;
        final String ip;
        final String ua;
        final long expiresAt;

        Meta(String username, long issuedAt, String ip, String ua, long expiresAt) {
            this.username = username;
            this.issuedAt = issuedAt;
            this.ip = ip;
            this.ua = ua;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, Meta> tokens = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> tokensByUser = new ConcurrentHashMap<>();

    // TTL for refresh tokens: 7 days
    private final long refreshTtlMs = 7L * 24 * 60 * 60 * 1000;

    public String createRefreshToken(String username, String ip, String userAgent) {
        String token = UUID.randomUUID().toString();
        long now = Instant.now().toEpochMilli();
        long exp = now + refreshTtlMs;
        Meta meta = new Meta(username, now, ip == null ? "" : ip, userAgent == null ? "" : userAgent, exp);
        tokens.put(token, meta);
        tokensByUser.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(token);
        return token;
    }

    // compatibility overload (no metadata)
    public String createRefreshToken(String username) {
        return createRefreshToken(username, "", "");
    }

    public boolean validateRefreshToken(String token, String username) {
        if (token == null || token.isBlank()) return false;
        Meta m = tokens.get(token);
        if (m == null) return false;
        if (!m.username.equals(username)) return false;
        if (m.expiresAt < Instant.now().toEpochMilli()) {
            // expired - remove
            revokeRefreshToken(token);
            return false;
        }
        return true;
    }

    public String rotateRefreshToken(String oldToken, String username, String ip, String userAgent) {
        if (!validateRefreshToken(oldToken, username)) return null;
        // remove old
        revokeRefreshToken(oldToken);
        // create new
        return createRefreshToken(username, ip, userAgent);
    }

    public void revokeRefreshToken(String token) {
        if (token == null) return;
        Meta m = tokens.remove(token);
        if (m != null) {
            Set<String> s = tokensByUser.get(m.username);
            if (s != null) s.remove(token);
        }
    }

    public void revokeAllTokensForUser(String username) {
        if (username == null) return;
        Set<String> s = tokensByUser.remove(username);
        if (s != null) {
            for (String t : s) tokens.remove(t);
        }
    }

}
