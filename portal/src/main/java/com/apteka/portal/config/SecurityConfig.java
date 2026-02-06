package com.apteka.portal.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.apteka.portal.security.JwtTokenProvider;
import com.apteka.portal.services.ClientService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientService clientService;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(clientService)
            .passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build(); 
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // API доступ без авторизации
                .requestMatchers("/api/v1/auth/login").permitAll()  // Изменил путь
                // .requestMatchers("/api/v1/register").permitAll()
                // .requestMatchers("/api/v1/apteka").permitAll()
                .requestMatchers("api/v1/clients").permitAll()
                // .requestMatchers("/api/v1/group-task").permitAll()
                .requestMatchers("api/v1/group-client").permitAll()

                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // API endpoints - настраивайте безопасность отдельно
                // .requestMatchers("/api/v1/clients/**").hasRole("ADMIN") // Пример

                // Разрешаем ВСЮ статику
                .requestMatchers(
                        "/styles/**",
                        "/images/**",
                        "/js/**",
                        "/*.html"
                ).permitAll()

                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtAuthFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}

// Выносим JwtAuthFilter в отдельный файл
class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            if (jwtTokenProvider.validateToken(token)) {
                try {
                    String login = jwtTokenProvider.getLoginFromToken(token);
                    var roles = jwtTokenProvider.getRolesFromToken(token);
                    
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(
                            login, 
                            null, 
                            roles
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    logger.error("Не удалось загрузить пользователя из токена", e);
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}