package com.apteka.portal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.LoginRequestDTO;
import com.apteka.portal.dtos.request.LogoutRequestDTO;
import com.apteka.portal.dtos.request.RefreshRequestDTO;
import com.apteka.portal.dtos.response.AuthResponseDTO;
import com.apteka.portal.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Работа с авторизацией")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Авторизация")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Обновление токенов")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @Operation(summary = "Выход из системы")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO dto) {
        authService.logout(dto.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Выход из всех систем где был авторизован пользователь (для будущих микросервисов)")
    @PostMapping("/invalidate-all")
    public ResponseEntity<Void> invalidateAllSessions(Authentication authentication) {
        String username = authentication.getName();
        authService.invalidateAllSession(username);
        return ResponseEntity.noContent().build();
    }
}
