package com.apteka.portal.components.servicesecurity;


import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component
public class MainPageLinksSecurityService {
    public void validateCanCreate(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAction(AccountAction.CREATE_MAIN_PAGE_LINK)) {
            throw new AccessDeniedException("У вас не прав на создание ссылок на главной странице!");
        }
    }

    public void validateCanUpdate(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAction(AccountAction.UPDATE_MAIN_PAGE_LINK)) {
            throw new AccessDeniedException("У вас не прав на обновление ссылок на главной странице!");
        }
    }

    public void validateCanDelete(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAction(AccountAction.DELETE_MAIN_PAGE_LINK)) {
            throw new AccessDeniedException("У вас не прав на удаление ссылок на главной странице!");
        }
    }
}
