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
        if (!currentUser.hasRole(UserRole.AMBASSADOR)) {
            throw new AccessDeniedException("Только пользователи с ролью AMBASSADOR могут создавать новости");
        }

        if (!Objects.equals(currentUser.getUserGroup().getId(), dto.userGroupId())
                && !canManageNewsInAnotherGroup(currentUser)) {
            throw new AccessDeniedException(
                    "Только пользователи с ролью (BOSS и AMBASSADOR) или (SENIOR и AMBASSADOR) могут создавать новости в другие отделы");
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
            if (!newsCreator(currentUser, news) && !currentUser.hasAnyRole(UserRole.ADMIN) && !bossInGroup(currentUser, news)) {
                throw new AccessDeniedException("Вы не можете изменить новость к которой не имеете отношения");
            }
        }

    }

    private boolean canManageNewsInAnotherGroup(AppUserDetails currentUser) {
        return currentUser.hasAnyRole(UserRole.ADMIN, UserRole.BOSS, UserRole.SENIOR);
    }

    private boolean newsCreator(AppUserDetails currentUser, News news) {
        return Objects.equals(news.getAuthor().getId(), currentUser.getClientId());
    }

    private boolean bossInGroup(AppUserDetails currentUser, News news) {
        return currentUser.hasRole(UserRole.BOSS)
                && Objects.equals(currentUser.getUserGroup().getId(), news.getUserGroup().getId());
    }
}
