package com.furniro.AuthService.service.other;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.furniro.AuthService.database.entity.Account;
import com.furniro.AuthService.util.KeyLoader;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${auth.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${auth.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private String publicKeyLocation;

    @Value("${auth.jwt.private-key-location}")
    private String privateKeyLocation;

    @Value("${auth.jwt.issuer}")
    private String issuer;

    @Value("${auth.jwt.algorithm}")
    private String algorithm;

    private RSAPrivateKey privateKey;

    private RSAPublicKey publicKey;

    private final RedisService redisService;

    @PostConstruct
    private void initKeys() {
        try {
            this.privateKey = KeyLoader.loadPrivateKey(privateKeyLocation);
            this.publicKey = KeyLoader.loadPublicKey(publicKeyLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA key pair", e);
        }
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
        String token;
        try {
            token = Jwts.builder()
                    .subject(account.getUserName())
                    .claim("role", account.getRole())
                    .claim("type", tokenType)
                    .id(UUID.randomUUID().toString())
                    .issuer(issuer)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expirationTime))
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
        return token;
    }

    public boolean validateToken(String token, String tokenType) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenId = claims.getId();

            // Check if blacklisted in Redis
            if (redisService.isCaching("BLACKLISTED_TOKEN:" + tokenId)) {
                return false;
            }

            boolean isCorrectType = claims.get("type", String.class).equalsIgnoreCase(tokenType);
            boolean isNotExpired = !claims.getExpiration().before(new Date());

            return isCorrectType && isNotExpired;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}