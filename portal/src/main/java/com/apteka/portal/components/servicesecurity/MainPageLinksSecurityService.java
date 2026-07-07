package com.apteka.portal.components.servicesecurity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.MainPageLink;
import com.apteka.portal.models.UserRole;

@Component
public class MainPageLinksSecurityService {

    public void validateCanSelectNonActive(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.)) {
            
        }
    }

    public void validateCanSafeDelete(AppUserDetails currentUser, MainPageLink mainPageLink) {
        if (!mainPageLink.isActive()) {
            throw new AccessDeniedException("Ссылка уже неактивна!");
        }
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAnyAction(AccountAction.PERMANENT_DELETE_MAIN_PAGE_LINK,
                AccountAction.SAFE_DELETE_MAIN_PAGE_LINK)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на удаление группы ссылок");
    }

    public void validateRestoreAfterSafeDelete(AppUserDetails currentUser, MainPageLink mainPageLink) {
        if (mainPageLink.isActive()) {
            throw new AccessDeniedException("Ссылка уже активна!");
        }
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_ACTIVATE_MAIN_PAGE_LINK_AFTER_SAFE_DELETE)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав восстанавливать группы ссылок!");
    }

    public void validateCanPermanentDelete(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.PERMANENT_DELETE_MAIN_PAGE_LINK)) {
            return;
        }
        throw new AccessDeniedException("у вас не прав на безвозвратное удаление группы ссылок!");
    }
}
