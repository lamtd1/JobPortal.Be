package com.example.jobportal.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Value;

import java.security.Key;
import java.util.Date;

// Đây là phiên bản cơ bản JwtService là phiên bản bố đời
// Ko dùng, đáng để đọc

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Sinh Jwt cho user da login
    public String generateToken(String email) {
        return Jwts.builder()
                // sub - trong jwt payload
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Tach payload ra khoi jwt
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // validate jwt token
    public boolean validateToken(String token, String email) {
        return extractEmail(token).equals(email);
    }

}