package com.apteka.portal.services;

import org.springframework.security.core.context.SecurityContextHolder;

import com.apteka.portal.models.AppUserDetails;

public class SecurityUtils {
    public static AppUserDetails getCurrentUser() {

        var authentification = SecurityContextHolder.getContext().getAuthentication();
        if (authentification != null && authentification.getPrincipal() instanceof AppUserDetails) {
            return (AppUserDetails) authentification.getPrincipal();
        }

        return null;
    }
}
