package com.apteka.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.apteka.portal.components.CustomAccessDeniedHandler;
import com.apteka.portal.components.CustomAuthenticationEntryPoint;
import com.apteka.portal.components.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final CsrfCookieFilter csrfCookieFilter;

    @Value("${cookie.secure.flag:false}")
    private boolean cookieSecureFlag;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.secure(cookieSecureFlag).sameSite("Lax"));
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfRequestHandler)
                        // AUDIT-FIX: OTP endpoints без CSRF (login по-прежнему требует X-XSRF-TOKEN)
                        .ignoringRequestMatchers(
                                "/api/v1/auth/employee/password-reset/request",
                                "/api/v1/auth/employee/password-reset/confirm",
                                "/api/v1/auth/apteka/code-login/request",
                                "/api/v1/auth/apteka/code-login/confirm"))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/sse/subscribe").authenticated()
                        .requestMatchers("/api/v1/main-page-links/links").permitAll()
                        .requestMatchers("/api/v1/auth/csrf", "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        // AUDIT-FIX: OTP password-reset и code-login без аутентификации
                        .requestMatchers(
                                "/api/v1/auth/employee/password-reset/request",
                                "/api/v1/auth/employee/password-reset/confirm",
                                "/api/v1/auth/apteka/code-login/request",
                                "/api/v1/auth/apteka/code-login/confirm")
                        .permitAll()
                        .requestMatchers("/openapi.yaml", "/openapi/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/avatars/**").permitAll()
                        // AUDIT-FIX: разрешить Spring Boot error endpoint без аутентификации
                        .requestMatchers("/error").permitAll()
                        // AUDIT-FIX: health для мониторинга без JWT
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class) 
                .addFilterAfter(csrfCookieFilter, CsrfFilter.class)
                .exceptionHandling(exception -> exception
                        // AUDIT-FIX: JSON 401 вместо redirect/HTML для неаутентифицированных API-запросов
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
