package com.apteka.portal.components;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.NewsRequestDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component
public class NewsSecurityService {

    public void validateCanCreateNews(AppUserDetails currentUser, NewsRequestDTO dto) {
        if (!currentUser.hasRole(UserRole.AMBASSADOR)) {
            throw new AccessDeniedException("Только пользователи с ролью AMBASSADOR могут создавать новости");
        }

        if (!Objects.equals(currentUser.getUserGroup().getId(), dto.userGroupId()) && !currentUser.hasAnyRole(UserRole.ADMIN, UserRole.BOSS, UserRole.SENIOR)) {
            throw new AccessDeniedException("Только пользователи с ролью (BOSS и AMBASSADOR) или (SENIOR и AMBASSADOR) могут создавать новости в другие отделы");
        }
    }
}
