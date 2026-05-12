package com.apteka.portal.services;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.apteka.portal.models.AppUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_USER_ID = "userId";

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final SecretKey key;

    public String generateAccessToken(AppUserDetails user) {

        Object userId = user.getInternalId();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim(CLAIM_USER_ID, userId)
                .claim("roles", user.getRoles())
                .claim("type", user.getType())
                .claim("userGroupId", user.getUserGroup().getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(Authentication authentication) {
        return generateAccessToken((AppUserDetails) authentication.getPrincipal());
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}
