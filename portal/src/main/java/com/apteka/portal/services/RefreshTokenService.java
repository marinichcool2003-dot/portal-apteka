package com.apteka.portal.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public RefreshToken create(String userName, boolean rememberMe) {
        String tokenStr = UUID.randomUUID().toString();
        String key = REDIS_PREFIX + tokenStr;
        String userKey = USER_TOKENS_PREFIX + userName;
        String sessionId = UUID.randomUUID().toString();

        long expirationMillis = rememberMe ? jwtRefreshExpirationWithRemember : jwtRefreshExpiration;

        redisTemplate.opsForValue().set(key, userName, expirationMillis, TimeUnit.MILLISECONDS);

        redisTemplate.opsForSet().add(userKey, tokenStr);
        redisTemplate.expire(userKey, expirationMillis, TimeUnit.MILLISECONDS);

        String sessionKey = USER_SESSIONS_PREFIX + userName + ":" + sessionId;
        redisTemplate.opsForHash().put(sessionKey, "token", tokenStr);
        redisTemplate.opsForHash().put(sessionKey, "createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().put(sessionKey, "rememberMe", String.valueOf(rememberMe));
        redisTemplate.expire(sessionKey, expirationMillis, TimeUnit.MILLISECONDS);

        log.info("Created refresh token for user: {}, sessionId: {}, rememberMe: {}",
                userName, sessionId, rememberMe);

        return RefreshToken.builder()
                .token(tokenStr)
                .username(userName)
                .rememberMe(rememberMe)
                .build();
    }

    public RefreshToken verify(String token) {
        String key = REDIS_PREFIX + token;
        String username = redisTemplate.opsForValue().get(key);

        if (username == null) {
            log.warn("Invalid or expired refresh token: {}", token.substring(0, 8) + "...");
            throw new InvalidRefreshTokenException("Некорректный или просроченный REFRESH TOKEN");
        }

        log.debug("Verified refresh token for user: {}", username);

        return RefreshToken.builder()
                .token(token)
                .username(username)
                .build();
    }

    public void deleteByRefreshToken(String refreshToken) {
        String key = REDIS_PREFIX + refreshToken;

        String username = redisTemplate.opsForValue().get(key);

        if (username != null) {
            String userKey = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().remove(userKey, refreshToken);
            log.debug("Removed refresh token from user's list: {}", username);
            
            deleteSessionByToken(username, refreshToken);
        }

        redisTemplate.delete(key);
        log.debug("Deleted refresh token: {}", refreshToken.substring(0, 8) + "...");
    }

    public void deleteByUser(String username) {
        log.info("Deleting all refresh tokens for user: {}", username);
        long startTime = System.currentTimeMillis();

        try {
            String userKey = USER_TOKENS_PREFIX + username;

            Set<String> tokens = redisTemplate.opsForSet().members(userKey);

            if (tokens == null || tokens.isEmpty()) {
                log.info("No refresh tokens found for user: {}", username);
                return;
            }

            log.debug("Found {} refresh tokens for user: {}", tokens.size(), username);

            List<String> keysToDelete = tokens.stream()
                    .map(token -> REDIS_PREFIX + token)
                    .collect(Collectors.toList());

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("Deleted {} refresh token keys", keysToDelete.size());
            }

            redisTemplate.delete(userKey);

            deleteAllSessionsByUser(username);

            log.info("Successfully deleted all {} refresh tokens for user: {} in {} ms",
                    tokens.size(), username, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Error deleting refresh tokens for user: {}", username, e);
            throw new RuntimeException("Failed to delete refresh tokens for user: " + username, e);
        }
    }

    private void deleteAllSessionsByUser(String username) {
        String userKey = USER_TOKENS_PREFIX + username;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);
        
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        for (String token : tokens) {
            deleteSessionByToken(username, token);
        }
    }

    private void deleteSessionByToken(String username, String token) {
        String sessionPattern = USER_SESSIONS_PREFIX + username + ":*";
        Set<String> sessionKeys = redisTemplate.keys(sessionPattern);
        
        if (sessionKeys != null && !sessionKeys.isEmpty()) {
            for (String sessionKey : sessionKeys) {
                String storedToken = (String) redisTemplate.opsForHash().get(sessionKey, "token");
                if (token.equals(storedToken)) {
                    redisTemplate.delete(sessionKey);
                    log.debug("Deleted session for token: {}", token.substring(0, 8) + "...");
                    break;
                }
            }
        }
    }

    private static final String TOKEN_SESSION_PREFIX = "token_session:";
    
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
        String tokenSessionKey = TOKEN_SESSION_PREFIX + token;
        String sessionKey = redisTemplate.opsForValue().get(tokenSessionKey);
        
        if (sessionKey != null) {
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(tokenSessionKey);
            log.debug("Deleted session for token: {}", token.substring(0, 8) + "...");
        }
    }

    public void deleteByRefreshTokenFast(String refreshToken) {
        String key = REDIS_PREFIX + refreshToken;
        String username = redisTemplate.opsForValue().get(key);

        if (username != null) {
            String userKey = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().remove(userKey, refreshToken);
            deleteSessionByTokenFast(refreshToken);
        }

        redisTemplate.delete(key);
        log.debug("Deleted refresh token: {}", refreshToken.substring(0, 8) + "...");
    }

    public long getTokenCountForUser(String username) {
        String userKey = USER_TOKENS_PREFIX + username;
        Long size = redisTemplate.opsForSet().size(userKey);
        return size != null ? size : 0;
    }

    public boolean existsByRefreshToken(String token) {
        String key = REDIS_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}