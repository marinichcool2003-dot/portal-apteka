package com.apteka.portal.components;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;

@Component
public class SecurityUtils {
    private static AppUserDetails getCurrentUser() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserDetails) {
            return (AppUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    public static AppUserDetails getRequiredCurrentUser() {
        AppUserDetails user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Действие требует авторизации");
        }
        return user;
    }
}
