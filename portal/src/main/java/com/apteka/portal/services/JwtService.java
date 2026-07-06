package com.apteka.portal.services;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

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
                .claim("role", user.getRole())
                .claim("actions", user.getActions())
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

    public Integer extractUserGroupId(String token) {
        return extractClaim(token, claims -> claims.get("userGroupId", Integer.class));
    }

    public UserRole extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role", UserRole.class));
    }

    public Set<AccountAction> extractAccountActions(String token) {
        return extractClaim(token, claims -> {
            Collection<?> rawActions = claims.get("actions", Collection.class);
            if(rawActions == null) {
                return Set.of();
            }
            return rawActions.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(AccountAction::fromCode)
                    .collect(Collectors.toSet());
        });
    }
}
