package com.apteka.portal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.LoginRequestDTO;
import com.apteka.portal.dtos.request.RefreshRequestDTO;
import com.apteka.portal.dtos.response.AuthResponseDTO;
import com.apteka.portal.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        String username = authentication.getName();
        authService.logout(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invalidate-all")
    public ResponseEntity<Void> invalidateAllSessions(Authentication authentication) {
        String username = authentication.getName();
        authService.invalidateAllSession(username);
        return ResponseEntity.noContent().build();
    }
}
