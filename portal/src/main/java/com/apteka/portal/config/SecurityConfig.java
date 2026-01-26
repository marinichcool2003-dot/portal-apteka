package com.apteka.portal.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import com.apteka.portal.security.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("null")
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // API доступ без авторизации
                .requestMatchers("/api/v1/login", "/api/v1/register").permitAll()

                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                //API
                .requestMatchers("/api/v1/group-apteki/**").permitAll()
                .requestMatchers("/api/v1/task/**").permitAll()
                .requestMatchers("/api/v1/group-task/**").permitAll()
                .requestMatchers("/api/v1/apteka/**").permitAll()
                .requestMatchers("/api/v1/clients/**").permitAll()
                .requestMatchers("/api/v1/group-clients/**").permitAll()
                .requestMatchers("/api/v1/task-comments/**").permitAll()


                // Разрешаем ВСЮ статику
                .requestMatchers(
                        "/styles/**",
                        "/images/**",
                        "/js/**",
                        "/*.html"
                ).permitAll()

                .anyRequest().authenticated()
            )

            .addFilterBefore(new JwtAuthFilter(jwtTokenProvider),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    static class JwtAuthFilter extends OncePerRequestFilter {

        private final JwtTokenProvider jwtTokenProvider;

        public JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
            this.jwtTokenProvider = jwtTokenProvider;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    String login = jwtTokenProvider.getLoginFromToken(token);
                    var auth = new UsernamePasswordAuthenticationToken(login, null, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(request, response);
        }
    }
}
