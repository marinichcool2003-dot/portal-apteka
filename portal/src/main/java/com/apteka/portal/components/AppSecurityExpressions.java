package com.apteka.portal.components;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;

@Component("appSecurity")
public class AppSecurityExpressions {

    public boolean isClient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AppUserDetails)) {
            return false;
        }

        AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
        return userDetails.isClient();
    }
}
