package com.auth.app.util;

import com.auth.app.entity.Role;
import com.auth.app.entity.User;
import com.auth.app.entity.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());

        if (user.getRole()!=null){
            claims.put("role", user.getRole().name());
        }
        if (user.getUserType()!=null){
            claims.put("userType", user.getUserType().name());
        }

        if (user.getCompany() != null) {
            claims.put("companyId", user.getCompany().getId().toString());
        }

        if (user.getCustomer() != null) {
            claims.put("customerId", user.getCustomer().getId().toString());
        }

        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("tokenType", "refresh");

        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userIdStr);
    }

    public Role extractRole(String token) {
        String roleStr = extractClaim(token, claims -> claims.get("role", String.class));
        return Role.valueOf(roleStr);
    }

    public UserType extractUserType(String token) {
        String userTypeStr = extractClaim(token, claims -> claims.get("userType", String.class));
        return UserType.valueOf(userTypeStr);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // SecretKey ile doğrulama
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
