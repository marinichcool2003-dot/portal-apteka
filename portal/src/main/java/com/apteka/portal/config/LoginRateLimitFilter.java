package com.apteka.portal.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    public LoginRateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/api/v1/auth/login".equals(request.getRequestURI())) {
            String ip = request.getRemoteAddr();
            String redisKey = "rate:login:" + ip;

            Long currentRequests = redisTemplate.opsForValue().increment(redisKey, 1);
            
            if (currentRequests != null && currentRequests == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(1));
            }

            if (currentRequests != null && currentRequests > 5) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Слишком много попыток входа. Повторите попытку позже.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

