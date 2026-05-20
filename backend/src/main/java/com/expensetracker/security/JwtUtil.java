package com.expensetracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private volatile Key key;

    private Key getKey() {
        if (key != null) return key;
        synchronized (this) {
            if (key != null) return key;
            try {
                if (secret != null && secret.getBytes().length >= 32) {
                    key = Keys.hmacShaKeyFor(secret.getBytes());
                } else {
                    // generate a secure random key for HS256 and warn
                    key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                    System.err.println("WARNING: jwt.secret is missing or too short — generated a temporary key. Set JWT_SECRET env var for persistent tokens.");
                }
            } catch (IllegalArgumentException e) {
                // fallback to generated secure key
                key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                System.err.println("WARNING: jwt.secret invalid — generated a temporary key. Set JWT_SECRET env var for persistent tokens.");
            }
            return key;
        }
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
