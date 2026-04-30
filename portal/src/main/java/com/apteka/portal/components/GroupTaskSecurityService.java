package com.apteka.portal.components;

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

        if (isAdmin || isBossOfGroup) {
            return;
        }
        throw new AccessDeniedException("Создать списки работ могут только начальники своего отдела");
    }
}
