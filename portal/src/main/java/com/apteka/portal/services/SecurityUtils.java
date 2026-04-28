package com.apteka.portal.services;

import org.springframework.security.core.context.SecurityContextHolder;

import com.apteka.portal.models.AppUserDetails;

public class SecurityUtils {
    public static AppUserDetails getCurrentUser() {
        return (AppUserDetails) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
    }
}
