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
import com.apteka.portal.dtos.request.auth.AptekaCodeLoginConfirmDTO;
import com.apteka.portal.dtos.request.auth.AptekaCodeLoginRequestDTO;
import com.apteka.portal.dtos.request.auth.EmployeePasswordResetConfirmDTO;
import com.apteka.portal.dtos.request.auth.EmployeePasswordResetRequestDTO;
import com.apteka.portal.dtos.response.AuthResponseDTO;
import com.apteka.portal.dtos.response.auth.OtpRequestResponseDTO;
import com.apteka.portal.exceptions.InvalidRefreshTokenException;
import com.apteka.portal.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Аутентификация", description = "Вход, обновление токенов, выход и инвалидация сессий")
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

    @Operation(summary = "Инициализация CSRF", description = "Эндпоинт для получения CSRF-токена клиентом (bootstrap). Возвращает токен в теле и cookie XSRF-TOKEN.")
    @GetMapping("/csrf")
    // AUDIT-FIX: возвращаем token в JSON — cookie иногда сбрасывается после login, SPA/скриптам нужен явный токен
    public ResponseEntity<java.util.Map<String, String>> csrfBootstrap(
            org.springframework.security.web.csrf.CsrfToken csrfToken) {
        return ResponseEntity.ok(java.util.Map.of(
                "token", csrfToken.getToken(),
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName()));
    }

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и устанавливает access/refresh cookies.")
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

    @Operation(summary = "Обновить токены", description = "Обновляет access и refresh токены по refresh-cookie. При невалидном токене удаляет cookies.")
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

    @Operation(summary = "Выход из системы", description = "Завершает текущую сессию и удаляет auth cookies.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.extractToken(request, CookieUtils.REFRESH_TOKEN_COOKIE);
        authService.logout(refreshToken);
        cookieUtils.deleteAuthCookies(response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Инвалидировать все сессии", description = "Завершает все сессии текущего пользователя и удаляет auth cookies.")
    @PostMapping("/invalidate-all")
    public ResponseEntity<Void> invalidateAllSessions(Authentication authentication, HttpServletResponse response) {
        String username = authentication.getName();
        authService.invalidateAllSession(username);
        cookieUtils.deleteAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    // AUDIT-FIX: OTP сброс пароля сотрудника — запрос кода
    @Operation(summary = "Запрос сброса пароля сотрудника", description = "Отправляет OTP-код на email сотрудника. Ответ не раскрывает существование учётной записи.")
    @PostMapping("/employee/password-reset/request")
    public ResponseEntity<OtpRequestResponseDTO> requestEmployeePasswordReset(
            @Valid @RequestBody EmployeePasswordResetRequestDTO dto) {
        return ResponseEntity.ok(authService.requestEmployeePasswordReset(dto));
    }

    // AUDIT-FIX: OTP сброс пароля сотрудника — подтверждение
    @Operation(summary = "Подтверждение сброса пароля сотрудника", description = "Подтверждает OTP-код и устанавливает новый пароль. Инвалидирует все сессии.")
    @PostMapping("/employee/password-reset/confirm")
    public ResponseEntity<Void> confirmEmployeePasswordReset(
            @Valid @RequestBody EmployeePasswordResetConfirmDTO dto) {
        authService.confirmEmployeePasswordReset(dto);
        return ResponseEntity.ok().build();
    }

    // AUDIT-FIX: OTP вход аптеки — запрос кода
    @Operation(summary = "Запрос входа аптеки по коду", description = "Отправляет OTP-код на email аптеки. Ответ не раскрывает существование учётной записи.")
    @PostMapping("/apteka/code-login/request")
    public ResponseEntity<OtpRequestResponseDTO> requestAptekaCodeLogin(
            @Valid @RequestBody AptekaCodeLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.requestAptekaCodeLogin(dto));
    }

    // AUDIT-FIX: OTP вход аптеки — подтверждение и установка cookies
    @Operation(summary = "Подтверждение входа аптеки по коду", description = "Подтверждает OTP-код и устанавливает access/refresh cookies.")
    @PostMapping("/apteka/code-login/confirm")
    public ResponseEntity<Void> confirmAptekaCodeLogin(
            @Valid @RequestBody AptekaCodeLoginConfirmDTO dto,
            HttpServletResponse response) {
        AuthResponseDTO authDTO = authService.confirmAptekaCodeLogin(dto);

        int refreshExpirySec = (int) (jwtRefreshExpiration / 1000);
        int accessExpirySec = (int) (jwtExpiration / 1000);

        cookieUtils.createAccessCookie(response, authDTO.accessToken(), accessExpirySec);
        cookieUtils.createRefreshCookie(response, authDTO.refreshToken(), refreshExpirySec);

        return ResponseEntity.ok().build();
    }
}
