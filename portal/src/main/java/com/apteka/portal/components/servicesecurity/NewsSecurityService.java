package com.apteka.portal.components.servicesecurity;

import com.apteka.portal.dtos.response.WorkTypeResponseDTO.PriorityResponseDTO;
import java.util.Objects;
import java.util.UUID;

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
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        boolean sameGroup = sameGroup(currentUser, dto.userGroupId());

        if (sameGroup && !currentUser.hasAnyAction(AccountAction.NEWS_WORK, AccountAction.NEWS_WORK_ALL_GROUPS)) {
            throw new AccessDeniedException("У вас нет прав доступа для работы с новостями!");
        }
        if (!sameGroup && !currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)) {
            throw new AccessDeniedException("У вас нет прав доступа для работы с новостями в других группах!");
        }
    }

    public void validateCanUpdate(AppUserDetails currentUser, News news) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        UUID authorId = news.getAuthor().getId();
        Integer creatoeGroupId = news.getAuthor().getAccount().getUserGroup().getId();
        boolean sameGroup = sameGroup(currentUser, creatoeGroupId);
        boolean isAuthor = isAuthor(currentUser, authorId);

        if (!isAuthor && sameGroup && !currentUser.hasAction(AccountAction.UPDATE_ALL_NEWS_CREATE_GROUP)) {
            throw new AccessDeniedException("У вас нет прав на изменение чужих новостей");
        }

        if (!isAuthor && !sameGroup && !currentUser.hasAction(AccountAction.UPDATE_ALL_NEWS)) {
            throw new AccessDeniedException("У вас нет прав на изменение чужих новостей");
        }

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
        return Objects.equals(currentUser.getInternalId(), news.getAuthor().getId());
    }

    private boolean sameGroup(AppUserDetails currentUser, Integer userGroupId) {
        return Objects.equals(currentUser.getUserGroup().getId(), userGroupId);
    }

    private boolean isAuthor(AppUserDetails currentUser, UUID authorId) {
        return Objects.equals(currentUser.getInternalId(), authorId);
    }
}
