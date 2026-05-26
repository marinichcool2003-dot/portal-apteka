package com.apteka.portal.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.InvalidRefreshTokenException;
import com.apteka.portal.models.RefreshToken;
import com.apteka.portal.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;

    @Value("${jwt.refresh.expiration-with-remember}")
    private long jwtRefreshExpirationWithRemember;

    public RefreshToken create(String userName, boolean rememberMe) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(userName);
        token.setRememberMe(rememberMe);
        token.setExpiryDate(
                Instant.now().plusMillis(rememberMe ? jwtRefreshExpirationWithRemember : jwtRefreshExpiration));
        return repository.save(token);
    }

    public RefreshToken verify(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Некорректный REFRESH TOKEN"));
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Просроченный REFRESH TOKEN");
        }
        return refreshToken;
    }

    public void deleteByUser(String username) {
        repository.deleteByUsername(username);
    }

    public void deleteByRefreshToken(String refreshtoken) {
        repository.deleteByToken(refreshtoken);
    }
}
