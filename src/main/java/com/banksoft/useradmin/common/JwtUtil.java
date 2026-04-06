package com.banksoft.useradmin.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiry-ms}")
    private long expiryMs;

    @Value("${app.jwt.refresh-expiry-ms}")
    private long refreshExpiryMs;

    private SecretKey getKey() {
        byte[] keyBytes = secret.getBytes();
        // pad to at least 64 bytes for HS512
        if (keyBytes.length < 64) {
            byte[] padded = new byte[64];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userId, String username, List<String> roles) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("username", username)
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object roles = parseToken(token).get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }
}
