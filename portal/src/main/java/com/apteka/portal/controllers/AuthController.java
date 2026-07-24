package com.apteka.portal.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.components.CookieUtils;
import com.apteka.portal.dtos.request.LoginRequestDTO;
import com.apteka.portal.dtos.request.RefreshRequestDTO;
import com.apteka.portal.dtos.response.AuthResponseDTO;
import com.apteka.portal.exceptions.InvalidRefreshTokenException;
import com.apteka.portal.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;

    @Value("${jwt.refresh.expiration-with-remember}")
    private long jwtRefreshExpirationWithRemember;

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrfBootstrap() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDTO dto, HttpServletResponse response) {
        AuthResponseDTO authDTO = authService.login(dto);

        long refreshExpiryMs = authDTO.rememberMe() ? jwtRefreshExpirationWithRemember : jwtRefreshExpiration;
        int refreshExpirySec = (int) (refreshExpiryMs / 1000);
        int accessExpirySec = (int) (jwtExpiration / 1000);

        cookieUtils.createAccessCookie(response, authDTO.accessToken(), accessExpirySec);
        cookieUtils.createRefreshCookie(response, authDTO.refreshToken(), refreshExpirySec);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = cookieUtils.extractToken(request, CookieUtils.REFRESH_TOKEN_COOKIE);

        if (oldRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            RefreshRequestDTO refreshRequest = new RefreshRequestDTO(oldRefreshToken);
            AuthResponseDTO authDto = authService.refresh(refreshRequest);

            long refreshExpiryMs = authDto.rememberMe() ? jwtRefreshExpirationWithRemember : jwtRefreshExpiration;
            int refreshExpirySec = (int) (refreshExpiryMs / 1000);
            int accessExpirySec = (int) (jwtExpiration / 1000);

            cookieUtils.createAccessCookie(response, authDto.accessToken(), accessExpirySec);
            cookieUtils.createRefreshCookie(response, authDto.refreshToken(), refreshExpirySec);

            return ResponseEntity.ok().build();

        } catch (InvalidRefreshTokenException e) {
            cookieUtils.deleteAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.extractToken(request, CookieUtils.REFRESH_TOKEN_COOKIE);
        authService.logout(refreshToken);
        cookieUtils.deleteAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invalidate-all")
    public ResponseEntity<Void> invalidateAllSessions(Authentication authentication, HttpServletResponse response) {
        String username = authentication.getName();
        authService.invalidateAllSession(username);
        cookieUtils.deleteAuthCookies(response);
        return ResponseEntity.noContent().build();
    }
}
