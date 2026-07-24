package com.apteka.portal.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final DefaultRedisScript<Long> LOGIN_RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]); "
                    + "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; "
                    + "return count;",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.login.max-requests:5}")
    private long maxRequests;

    @Value("${app.rate-limit.login.window-minutes:1}")
    private long windowMinutes;

    @Value("${app.rate-limit.login.fail-open:true}")
    private boolean failOpen;

    public LoginRateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/api/v1/auth/login".equals(request.getRequestURI())) {
            String ip = getClientIp(request);
            String redisKey = "rate:login:" + ip;

            try {
                Long currentRequests = redisTemplate.execute(
                        LOGIN_RATE_LIMIT_SCRIPT, List.of(redisKey), String.valueOf(windowMinutes * 60));

                if (currentRequests != null && currentRequests > maxRequests) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"Слишком много попыток входа. Повторите попытку позже.\"}");
                    return;
                }
            } catch (Exception e) {
                log.error("Unable to apply login rate limit for client {}", ip, e);
                if (!failOpen) {
                    throw new ServletException("Login rate limit is unavailable", e);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String forwardedIp = forwardedFor.split(",", 2)[0].trim();
            if (!forwardedIp.isEmpty()) {
                return forwardedIp;
            }
        }
        return request.getRemoteAddr();
    }
}

