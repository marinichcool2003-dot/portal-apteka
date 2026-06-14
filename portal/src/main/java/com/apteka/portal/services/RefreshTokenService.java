package com.apteka.portal.services;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.InvalidRefreshTokenException;
import com.apteka.portal.models.RefreshToken;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;

    @Value("${jwt.refresh.expiration-with-remember}")
    private long jwtRefreshExpirationWithRemember;

    private static final String REDIS_PREFIX = "refresh_token:";

    public RefreshToken create(String userName, boolean rememberMe) {
        String tokenStr = UUID.randomUUID().toString();
        String key = REDIS_PREFIX + tokenStr;

        long expirationMillis = rememberMe ? jwtRefreshExpirationWithRemember : jwtRefreshExpiration;

        redisTemplate.opsForValue().set(key, userName, expirationMillis, TimeUnit.MILLISECONDS);

        RefreshToken token = RefreshToken.builder()
                .token(tokenStr)
                .username(userName)
                .rememberMe(rememberMe)
                .build();

        return token;
    }

    public RefreshToken verify(String token) {
        String key = REDIS_PREFIX + token;
        String username = redisTemplate.opsForValue().get(key);

        if (username == null) {
            throw new InvalidRefreshTokenException("Некорректный или просроченный REFRESH TOKEN");
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUsername(username);
        return refreshToken;
    }

    public void deleteByRefreshToken(String refreshtoken) {
        String key = REDIS_PREFIX + refreshtoken;
        redisTemplate.delete(key);
    }

    public void deleteByUser(String username) {

        var keys = redisTemplate.keys(REDIS_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                if (username.equals(redisTemplate.opsForValue().get(key))) {
                    redisTemplate.delete(key);
                }
            }
        }
    }
}
