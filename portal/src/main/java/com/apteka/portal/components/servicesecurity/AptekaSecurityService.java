package com.apteka.portal.components.servicesecurity;

import org.springframework.security.access.AccessDeniedException;
import java.util.Objects;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component
public class AptekaSecurityService {

    public void validateCanSeeSaveDeleted(AppUserDetails currentUser, Boolean isActive) {
        if (!isActive && !currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может видеть удалённые аптеки!");
        }
    }

    public void validateCanSelectApteka(AppUserDetails currentUser, Integer aptekaGroupId) {
        if (currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAnyAction(
                        AccountAction.UPDATE_ALL_APTEKA,
                        AccountAction.UPDATE_APTEKA_ACCOUNT,
                        AccountAction.UPDATE_APTEKA_DESCRIPTION,
                        AccountAction.SAFE_DELETE_APTEKA,
                        AccountAction.PERMANENT_DELETE_APTEKA)
                || Objects.equals(currentUser.getUserGroup().getId(), aptekaGroupId)) {
            return;
        }
        throw new AccessDeniedException("Вы можете просматривать аптеки только своей группы");
    }

    public boolean canSelectAllAptekas(AppUserDetails currentUser) {
        return currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAnyAction(
                        AccountAction.UPDATE_ALL_APTEKA,
                        AccountAction.UPDATE_APTEKA_ACCOUNT,
                        AccountAction.UPDATE_APTEKA_DESCRIPTION,
                        AccountAction.SAFE_DELETE_APTEKA,
                        AccountAction.PERMANENT_DELETE_APTEKA);
    }

    public void validateCanCreateApteka(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAction(AccountAction.CREATE_APTEKA)) {
            throw new AccessDeniedException("Вы не можете создавать учетные записи аптек!");
        }
    }

    public void validateCanUpdateAccountApteka(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)
                && !currentUser.hasAnyAction(AccountAction.UPDATE_ALL_APTEKA, AccountAction.UPDATE_APTEKA_ACCOUNT)) {
            throw new AccessDeniedException("Вы не можете обновлять данные аккаунта аптек!");
        }
    }

    public void validateCanUpdateDescriptionApteka(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAnyAction(AccountAction.UPDATE_ALL_APTEKA,
                AccountAction.UPDATE_APTEKA_DESCRIPTION)) {
            throw new AccessDeniedException("Вы не можете обновлять описания аптек!");
        }
    }

    public void validateCanSafeDeleteApteka(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAnyAction(AccountAction.SAFE_DELETE_APTEKA, AccountAction.PERMANENT_DELETE_APTEKA)) {
            throw new AccessDeniedException("Вы не можете удалять аптеки!");
        }
    }

    public void validateCanPermanentDeleteApteka(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAction(AccountAction.PERMANENT_DELETE_APTEKA)) {
            throw new AccessDeniedException("Вы не можете удалять аптеки!");
        }
    }
}
