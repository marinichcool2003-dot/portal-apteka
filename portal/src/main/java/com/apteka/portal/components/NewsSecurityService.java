package com.apteka.portal.components;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.NewsRequestDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.News;
import com.apteka.portal.models.UserRole;

@Component
public class NewsSecurityService {

    public void validateCanCreateNews(AppUserDetails currentUser, NewsRequestDTO dto) {
        if (!currentUser.hasAnyRole(UserRole.AMBASSADOR, UserRole.SENIOR_AMBASSADOR)) {
            throw new AccessDeniedException("Только пользователи с ролью AMBASSADOR могут создавать новости");
        }

        if (!currentUser.hasRole(UserRole.SENIOR_AMBASSADOR) && !Objects.equals(currentUser.getUserGroup().getId(), dto.userGroupId())) {
            throw new AccessDeniedException(
                    "Только пользователи с ролью (SENIOR_AMBASSADOR) могут создавать новости в другие отделы");
        }
    }

    public void validateCanUpdate(AppUserDetails currentUser, News news) {
        if (!currentUser.hasRole(UserRole.AMBASSADOR)) {
            throw new AccessDeniedException("Только пользователи с ролью AMBASSADOR могут создавать новости");
        }
        if (!Objects.equals(news.getUserGroup().getId(), currentUser.getUserGroup().getId())) {
            if (!newsCreator(currentUser, news) && !currentUser.hasRole(UserRole.ADMIN)) {
                throw new AccessDeniedException("Вы не можете изменить новость к которой не имеете отношения");
            }
        } else {
            if (!newsCreator(currentUser, news) && !currentUser.hasAnyRole(UserRole.ADMIN) && !currentUser.hasRole(UserRole.SENIOR_AMBASSADOR)) {
                throw new AccessDeniedException("Вы не можете изменить новость к которой не имеете отношения");
            }
        }

    }

    private boolean newsCreator(AppUserDetails currentUser, News news) {
        return Objects.equals(news.getAuthor().getId(), currentUser.getClientId());
    }
}
