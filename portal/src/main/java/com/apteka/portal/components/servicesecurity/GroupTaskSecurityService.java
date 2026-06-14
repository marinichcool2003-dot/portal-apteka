package com.apteka.portal.components.servicesecurity;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;

@Component
public class GroupTaskSecurityService {
    public void validateBossOrAdminInGroup(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.getType() != UserType.CLIENT) {
            throw new AccessDeniedException("Недопустимый тип пользователя");
        }

        boolean isAdmin = currentUser.getRoles().contains(UserRole.ADMIN);
        boolean isBossOfGroup = currentUser.getRoles().contains(UserRole.BOSS)
                && Objects.equals(currentUser.getUserGroup().getId(), userGroup.getId());

        if (!isAdmin && !isBossOfGroup) {
            throw new AccessDeniedException("Создать списки работ могут только начальники своего отдела");
        }
    }

    public void validateCanUpdateOrDelete(AppUserDetails currentUser) {
        if (!currentUser.isClient()) {
            throw new AccessDeniedException("Недопустимый тип пользователя");
        }
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Изменять и удалять тип работ может только администратор!");
        }
    }
}
