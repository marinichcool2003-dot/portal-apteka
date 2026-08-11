package com.apteka.portal.components.servicesecurity;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.news.NewsRequestDTO;
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

        boolean sameGroup = sameGroupWithGroupWhereNews(currentUser, dto.userGroupId());

        if (!sameGroup) {
            if (currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)) {
                return;
            }
            throw new AccessDeniedException("У вас нет прав доступа для работы с новостями в других группах!");
        }

        if (currentUser.hasAction(AccountAction.NEWS_WORK)
                || currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)) {
            return;
        }

        throw new AccessDeniedException("У вас нет доступа для работы с новостями");
    }

    public void validateCanSelect(AppUserDetails currentUser, Integer newsGroupId) {
        if (currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)
                || (sameGroupWithGroupWhereNews(currentUser, newsGroupId)
                        && (currentUser.hasAction(AccountAction.NEWS_WORK)
                                // AUDIT-FIX: аптека может читать новости только своей группы
                                || currentUser.hasRole(UserRole.APTEKA)))) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на просмотр новостей этой группы");
    }

    public void validateCanUpdate(AppUserDetails currentUser, News news) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.UPDATE_ALL_NEWS)) {
            return;
        }

        UUID authorId = news.getAuthor().getId();
        boolean isAuthor = isAuthor(currentUser, authorId);

        if (isAuthor) {
            if (currentUser.hasAction(AccountAction.NEWS_WORK)
                    || currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)) {
                return;
            }
            throw new AccessDeniedException("У вас нет права на редактирование новости!");
        }

        Integer creatorGroupId = news.getAuthor().getAccount().getUserGroup().getId();
        boolean sameGroupWithAuthor = sameGroupWithAuthor(currentUser, creatorGroupId);

        if (sameGroupWithAuthor) {
            if (currentUser.hasAction(AccountAction.UPDATE_ALL_NEWS_CREATE_GROUP)) {
                return;
            }
        }

        Integer newsUserGroupId = news.getUserGroup().getId();
        boolean sameGroupWithGroupWhereNews = sameGroupWithGroupWhereNews(currentUser, newsUserGroupId);

        if (sameGroupWithGroupWhereNews) {
            if (currentUser.hasAction(AccountAction.UPDATE_ALL_NEWS_IN_GROUP)) {
                return;
            }

            if (sameGroupWithAuthor) {
                throw new AccessDeniedException(
                        "У вас нет прав на обновление новостей, созданных сотрудниками вашего отдела!");
            }

            throw new AccessDeniedException("У вас нет права на изменение чужих новостей!");
        }

        throw new AccessDeniedException("У вас нет прав на изменение новостей других групп!");
    }

    public void validateCanDelete(AppUserDetails currentUser, News news) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.DELETE_ALL_NEWS)) {
            return;
        }

        UUID authorId = news.getAuthor().getId();
        boolean isAutor = isAuthor(currentUser, authorId);

        if (isAutor) {
            if (currentUser.hasAction(AccountAction.NEWS_WORK)) {
                return;
            }
            if (currentUser.hasAction(AccountAction.NEWS_WORK_ALL_GROUPS)) {
                return;
            }
            throw new AccessDeniedException("У вас нет прав на удаление данной новости!");
        }

        Integer creatorGroupId = news.getAuthor().getAccount().getUserGroup().getId();
        boolean sameGroupWithAuthor = sameGroupWithAuthor(currentUser, creatorGroupId);

        if (sameGroupWithAuthor) {
            if (currentUser.hasAction(AccountAction.DELETE_ALL_NEWS_CREATE_GROUP)) {
                return;
            }
        }

        Integer newsUserGroupId = news.getUserGroup().getId();
        boolean sameGroupWithGroupWhereNews = sameGroupWithGroupWhereNews(currentUser, newsUserGroupId);

        if (sameGroupWithGroupWhereNews) {
            if (currentUser.hasAction(AccountAction.DELETE_ALL_NEWS_IN_GROUP)) {
                return;
            }

            if (sameGroupWithAuthor) {
                throw new AccessDeniedException(
                        "У вас нет прав на удаление новостей, созданных сотрудниками вашего отдела!");
            }
            throw new AccessDeniedException("У вас не прав на удаление чужих новостей в вашем отделе!");
        }

        throw new AccessDeniedException("У вас нет прав на удаление новостей других групп!");
    }

    private boolean sameGroupWithAuthor(AppUserDetails currentUser, Integer authorGroupId) {
        if (currentUser.getUserGroup() == null || authorGroupId == null) {
            return false;
        }
        return Objects.equals(currentUser.getUserGroup().getId(), authorGroupId);
    }

    private boolean sameGroupWithGroupWhereNews(AppUserDetails currentUser, Integer newsUserGroupId) {
        if (currentUser.getUserGroup() == null || newsUserGroupId == null) {
            return false;
        }
        return Objects.equals(currentUser.getUserGroup().getId(), newsUserGroupId);
    }

    private boolean isAuthor(AppUserDetails currentUser, UUID authorId) {
        return Objects.equals(currentUser.getInternalId(), authorId);
    }
}
