package com.apteka.portal.components;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component("security")
public class SecurityExpressionHandler {
    public boolean hasAction(String actionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AppUserDetails userDetails)) {
            return false;
        }

        try {
            AccountAction action = AccountAction.valueOf(actionName);
            return userDetails.hasAction(action);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasAnyAction(String... actionNames) {
        for (String actionName : actionNames) {
            if (hasAction(actionName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AppUserDetails userDetails)) {
            return false;
        }

        try {
            UserRole role = UserRole.valueOf(roleName);
            return userDetails.hasRole(role);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public AppUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails) {
            return (AppUserDetails) principal;
        }
        return null;
    }
}
