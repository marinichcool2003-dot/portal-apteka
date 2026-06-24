package com.apteka.portal.components.servicesecurity;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.NewsRequestDTO;
import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.News;
import com.apteka.portal.models.UserRole;

@Component
public class NewsSecurityService {

    public void validateCanCreateNews(AppUserDetails currentUser, NewsRequestDTO dto) {

        boolean sameGroup = Objects.equals(currentUser.getUserGroup().getId(), dto.userGroupId());

        if (sameGroup && !currentUser.hasAnyAction(AccountAction.NEWS_WORK, AccountAction.NEWS_WORK_ALL_GROUPS)) {
            throw new AccessDeniedException("У вас нет прав доступа для работы с новостями!");
        }
        if (!sameGroup && !currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)) {
            throw new AccessDeniedException("У вас нет прав доступа для работы с новостями в других группах!");
        }
    }

    public void validateCanUpdate(AppUserDetails currentUser, News news) {
        if (!currentUser.hasAnyRole(UserRole.AMBASSADOR, UserRole.SENIOR_AMBASSADOR)) {
            throw new AccessDeniedException("Только пользователи с ролью AMBASSADOR могут изменять новости");
        }
        if (!Objects.equals(news.getUserGroup().getId(), currentUser.getUserGroup().getId())) {
            if (!newsCreator(currentUser, news) && !currentUser.hasRole(UserRole.ADMIN)) {
                throw new AccessDeniedException("Вы не можете изменить новость к которой не имеете отношения");
            }
        } else {
            if (!newsCreator(currentUser, news) && !currentUser.hasAnyRole(UserRole.ADMIN)
                    && !currentUser.hasRole(UserRole.SENIOR_AMBASSADOR)) {
                throw new AccessDeniedException("Вы не можете изменить новость к которой не имеете отношения");
            }
        }

    }

    private boolean newsCreator(AppUserDetails currentUser, News news) {
        return Objects.equals(news.getAuthor().getId(), currentUser.getInternalId());
    }
}
