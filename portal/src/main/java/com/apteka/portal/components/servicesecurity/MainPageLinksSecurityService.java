package com.apteka.portal.components.servicesecurity;


import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component
public class MainPageLinksSecurityService {
    public void validateCanCreateAndUpdate(AppUserDetails currentUser) {
        if (!currentUser.hasAnyRole(UserRole.ADMIN, UserRole.LINK_CHANGER)) {
            throw new AccessDeniedException("Только пользователи LINK_CHANGER и ADMIN могут добавлять ссылки на страницу");
        }
    }

    public void validateCanDelete(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только пользователь с ролью ADMIN может удалять группы ссылок на странице");
        }
    }
}
