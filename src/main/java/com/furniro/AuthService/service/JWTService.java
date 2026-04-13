package com.furniro.AuthService.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.furniro.AuthService.database.entity.Account;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_ACCESS_EXPIRATION}")
    private long accessExpiration;

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Account account, String tokenType) {

        long expirationTime = tokenType.equalsIgnoreCase("ACCESS")
                ? accessExpiration
                : refreshExpiration;

        return Jwts.builder()
                .subject(account.getUserName())
                .claim("role", account.getRole())
                .claim("type", tokenType)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token, String tokenType) {
        try {
            Claims claims = extractAllClaims(token);

            boolean isCorrectType = claims.get("type").toString().equalsIgnoreCase(tokenType);
            boolean isNotExpired = !claims.getExpiration().before(new Date());

            return isCorrectType && isNotExpired;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}