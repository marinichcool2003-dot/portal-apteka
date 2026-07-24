package com.apteka.portal.services;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.InvalidRefreshTokenException;
import com.apteka.portal.models.RefreshToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;

    @Value("${jwt.refresh.expiration-with-remember}")
    private long jwtRefreshExpirationWithRemember;

    private static final String REDIS_PREFIX = "refresh_token:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final String TOKEN_SESSION_PREFIX = "token_session:";

    public RefreshToken create(String userName, boolean rememberMe) {
        return createWithSession(userName, rememberMe);
    }

    public RefreshToken verify(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidRefreshTokenException("Некорректный или просроченный REFRESH TOKEN");
        }

        String key = REDIS_PREFIX + token;
        String username = redisTemplate.opsForValue().get(key);

        if (username == null) {
            log.warn("Invalid or expired refresh token: {}", tokenPrefix(token));
            throw new InvalidRefreshTokenException("Некорректный или просроченный REFRESH TOKEN");
        }

        String sessionKey = redisTemplate.opsForValue().get(TOKEN_SESSION_PREFIX + token);
        String rememberMe = sessionKey == null
                ? null
                : (String) redisTemplate.opsForHash().get(sessionKey, "rememberMe");
        if (rememberMe == null) {
            log.warn("Refresh token has no active session: {}", tokenPrefix(token));
            throw new InvalidRefreshTokenException("Некорректный или просроченный REFRESH TOKEN");
        }

        log.debug("Verified refresh token for user: {}", username);

        return RefreshToken.builder()
                .token(token)
                .username(username)
                .rememberMe(Boolean.parseBoolean(rememberMe))
                .build();
    }

    public void deleteByRefreshToken(String refreshToken) {
        deleteByRefreshTokenFast(refreshToken);
    }

    public void deleteByUser(String username) {
        log.info("Deleting all refresh tokens for user: {}", username);
        long startTime = System.currentTimeMillis();

        try {
            String userKey = USER_TOKENS_PREFIX + username;
            Set<String> tokens = redisTemplate.opsForSet().members(userKey);
            if (tokens == null || tokens.isEmpty()) {
                redisTemplate.delete(userKey);
                log.info("No refresh tokens found for user: {}", username);
                return;
            }

            log.debug("Found {} refresh tokens for user: {}", tokens.size(), username);
            for (String token : tokens) {
                deleteSessionByTokenFast(token);
                redisTemplate.delete(REDIS_PREFIX + token);
            }
            redisTemplate.delete(userKey);

            log.info("Successfully deleted all {} refresh tokens for user: {} in {} ms",
                    tokens.size(), username, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Error deleting refresh tokens for user: {}", username, e);
            throw new RuntimeException("Failed to delete refresh tokens for user: " + username, e);
        }
    }

    public RefreshToken createWithSession(String userName, boolean rememberMe) {
        String tokenStr = UUID.randomUUID().toString();
        String key = REDIS_PREFIX + tokenStr;
        String userKey = USER_TOKENS_PREFIX + userName;
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = USER_SESSIONS_PREFIX + userName + ":" + sessionId;
        String tokenSessionKey = TOKEN_SESSION_PREFIX + tokenStr;

        long expirationMillis = rememberMe ? jwtRefreshExpirationWithRemember : jwtRefreshExpiration;

        redisTemplate.opsForValue().set(key, userName, expirationMillis, TimeUnit.MILLISECONDS);
        
        redisTemplate.opsForValue().set(tokenSessionKey, sessionKey, expirationMillis, TimeUnit.MILLISECONDS);

        redisTemplate.opsForSet().add(userKey, tokenStr);
        redisTemplate.expire(userKey, expirationMillis, TimeUnit.MILLISECONDS);

        redisTemplate.opsForHash().put(sessionKey, "token", tokenStr);
        redisTemplate.opsForHash().put(sessionKey, "createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().put(sessionKey, "rememberMe", String.valueOf(rememberMe));
        redisTemplate.expire(sessionKey, expirationMillis, TimeUnit.MILLISECONDS);

        log.info("Created refresh token with session for user: {}, sessionId: {}", userName, sessionId);

        return RefreshToken.builder()
                .token(tokenStr)
                .username(userName)
                .rememberMe(rememberMe)
                .build();
    }

    public void deleteSessionByTokenFast(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        String tokenSessionKey = TOKEN_SESSION_PREFIX + token;
        String sessionKey = redisTemplate.opsForValue().get(tokenSessionKey);
        if (sessionKey != null) {
            redisTemplate.delete(sessionKey);
        }
        redisTemplate.delete(tokenSessionKey);
        log.debug("Deleted session for token: {}", tokenPrefix(token));
    }

    public void deleteByRefreshTokenFast(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String key = REDIS_PREFIX + refreshToken;
        String username = redisTemplate.opsForValue().get(key);

        if (username != null) {
            String userKey = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().remove(userKey, refreshToken);
        }

        deleteSessionByTokenFast(refreshToken);
        redisTemplate.delete(key);
        log.debug("Deleted refresh token: {}", tokenPrefix(refreshToken));
    }

    public long getTokenCountForUser(String username) {
        String userKey = USER_TOKENS_PREFIX + username;
        Long size = redisTemplate.opsForSet().size(userKey);
        return size != null ? size : 0;
    }

    public boolean existsByRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String key = REDIS_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    private String tokenPrefix(String token) {
        return token.length() <= 8 ? token : token.substring(0, 8) + "...";
    }
}