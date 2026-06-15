package com.apteka.portal.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtils {
    public static final String ACCESS_TOKEN_COOKIE = "X-Access-Token";
    public static final String REFRESH_TOKEN_COOKIE = "X-Refresh-Token";

    @Value("${cookie.secure.flag}")
    private boolean cookieSecureFlag;

    public void createAccessCookie(HttpServletResponse response, String token, int durationSeconds) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecureFlag)
                .path("/")
                .maxAge(durationSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void createRefreshCookie(HttpServletResponse response, String refreshToken, int durationSeconds) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(cookieSecureFlag)
                .path("/")
                .maxAge(durationSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteAuthCookies(HttpServletResponse response) {
        clearCookie(response, ACCESS_TOKEN_COOKIE, "/");
        clearCookie(response, REFRESH_TOKEN_COOKIE, "/");
    }

    public String extractToken(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecureFlag)
                .path(path)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
