package com.keepcalm.placementportal.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.keepcalm.placementportal.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder().subject(user.getId().toString())
                .claim("uid", user.getId())
                .claim("iid", user.getInstitution().getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name()).id(jti)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        // username is stored as the JWT subject in the current token format
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("uid", Long.class);
    }

    public Long extractInstitutionId(String token) {
        return extractAllClaims(token).get("iid", Long.class);
    }

    public Instant extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }

    public Duration remainingLifetime(String token) {
        return Duration.between(Instant.now(), extractExpiration(token));
    }

    public boolean validateToken(String token, String username) {
        String extractedUserId = extractUsername(token);
        return extractedUserId.equals(username) && !isTokenExpired(token);
    }

    public long getExpirationMillis() {
        return expiration;
    }

    public boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
