package in.nivasio.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT Token Provider — HS512, 15min access, 7d refresh.
 * Tokens contain: userId, tenantId, role, propertyId.
 * tenantId is ALWAYS read from JWT — NEVER from request body.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiry-ms:900000}") // 15 minutes
    private long accessExpiryMs;

    @Value("${app.jwt.refresh-expiry-ms:604800000}") // 7 days
    private long refreshExpiryMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Ensure key is long enough for HS512 (64 bytes minimum)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            byte[] padded = new byte[64];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 64));
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userId, String tenantId, String role, String propertyId) {
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of(
                        "tenantId", tenantId,
                        "role", role,
                        "propertyId", propertyId != null ? propertyId : "",
                        "type", "access"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiryMs))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(String userId, String tenantId) {
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of(
                        "tenantId", tenantId,
                        "type", "refresh"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired");
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String getTenantId(String token) {
        return parseToken(token).get("tenantId", String.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public String getPropertyId(String token) {
        return parseToken(token).get("propertyId", String.class);
    }

    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }

    public long getRefreshExpiryMs() {
        return refreshExpiryMs;
    }
}
