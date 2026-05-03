package com.apteka.portal.components;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component
public class UserGroupSecurityService {
    public void checkCanCreateGroup(AppUserDetails user) {

        if (user == null) {
            throw new AccessDeniedException("Пользователь не авторизован");
        }

        if (!user.getRoles().contains(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор управляет отделами");
        }
    }
}
