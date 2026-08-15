package com.apteka.portal.components.servicesecurity;

import com.apteka.portal.models.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class AptekaSecurityService {

    public void validateCanSeeSaveDeleted(AppUserDetails currentUser, Boolean isActive) {
        if (!isActive && !currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может видеть удалённые аптеки!");
        }
    }

    public void validateCanSelectApteka(AppUserDetails currentUser, Account account) {
        if (currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAnyAction(
                        AccountAction.UPDATE_ALL_APTEKA,
                        AccountAction.UPDATE_APTEKA_ACCOUNT,
                        AccountAction.UPDATE_APTEKA_DESCRIPTION,
                        AccountAction.SAFE_DELETE_APTEKA,
                        AccountAction.PERMANENT_DELETE_APTEKA)
                || sameGroup(currentUser, account)) {
            return;
        }
        throw new AccessDeniedException("Вы можете просматривать аптеки только своей группы");
    }

    public void canSelectAllAptekas(AppUserDetails currentUser, Set<Integer> requestedGroupIds) {
        if (currentUser.getType().equals(UserType.CLIENT)) {
            return;
        }
        if (requestedGroupIds == null || requestedGroupIds.isEmpty()) {
            return;
        }
        Set<Integer> allowedGroupIds  = currentUser.getRelations().keySet().stream()
                .map(UserGroup::getId).collect(Collectors.toSet());
        if (!allowedGroupIds .containsAll(requestedGroupIds)) {
            throw new AccessDeniedException("Вам запрещено просматривать аптеки в некоторых группах");
        }
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

    private boolean sameGroup(AppUserDetails currentUser, Account account) {
        Set<UserGroup> allGroups = account.getRelations().stream()
                .map(AccountRelation::getUserGroup).collect(Collectors.toSet());

        for (UserGroup userGroup : currentUser.getRelations().keySet()) {
            if (allGroups.contains(userGroup)) {
                return true;
            }
        }
        return false;
    }
}
