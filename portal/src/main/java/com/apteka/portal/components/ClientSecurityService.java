package com.apteka.portal.components;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;

@Component
public class ClientSecurityService {
    public void validateHasElevatedPrivelegesInGroup(AppUserDetails currentUser, Integer userGroupId) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAnyRole(UserRole.BOSS, UserRole.SENIOR, UserRole.USER)
                && Objects.equals(currentUser.getUserGroup().getId(), userGroupId)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на просмотр данных сотрудника");
    }

    public void validateWhoCanSelectClients(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.APTEKA)) {
            throw new AccessDeniedException("У вас нет прав на просмотр данных сотрудника");
        }
    }

    public void validateCanCreateClient(AppUserDetails currentUser, Integer userGroupId) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAnyRole(UserRole.BOSS)
                && Objects.equals(currentUser.getUserGroup().getId(), userGroupId)) {
            return;
        }
        throw new AccessDeniedException("У вас нет права создавать сотрудников");
    }

    public void canGiveRoleToClient(Set<UserRole> newRoles, AppUserDetails currentUser, UserGroup targetGroup) {

        if (newRoles == null || newRoles.isEmpty()) {
            throw new IllegalArgumentException("Роль обязательна");
        }

        UserRole maxRole = currentUser.getRoles().stream()
                .max(Comparator.comparingInt(UserRole::getLevel))
                .orElse(UserRole.USER);

        if (maxRole == UserRole.USER) {
            throw new AccessDeniedException("USER не может назначать роли");
        }

        if (newRoles.contains(UserRole.APTEKA)) {
            throw new AccessDeniedException("Пользователю нельзя присвоить роль аптеки");
        }

        boolean sameGroup = Objects.equals(currentUser.getUserGroup().getId(), targetGroup.getId());

        for (UserRole role : newRoles) {
            if (role.getLevel() >= maxRole.getLevel()) {
                throw new AccessDeniedException("Нельзя назначать роль выше или равную своей");
            }
            if (maxRole != UserRole.ADMIN && !sameGroup) {
                throw new AccessDeniedException("Можно работать только в своей группе");
            }
        }
    }
}
