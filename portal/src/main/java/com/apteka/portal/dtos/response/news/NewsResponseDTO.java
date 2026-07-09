package com.apteka.portal.dtos.response.news;

import java.time.Instant;
import java.util.Optional;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.News;
import com.apteka.portal.models.UserGroup;

public record NewsResponseDTO(
    Integer id,
    String title,
    String newsText,
    String authorName,
    UserGroupShortResponseDTO userGroup,
    Instant creationDate,
    Instant updatedAt,
    String updatedBy
) {
    public static NewsResponseDTO from(News news) {
        return new NewsResponseDTO(
            news.getId(), 
            news.getTitle(), 
            news.getNewsText(),
            Optional.ofNullable(news.getAuthor()).map(author -> author.getFullName()).orElse(null),
            new UserGroupShortResponseDTO(
                Optional.ofNullable(news.getUserGroup()).map(UserGroup::getId).orElse(null),
                Optional.ofNullable(news.getUserGroup()).map(UserGroup::getName).orElse(null)),
            news.getCreationDate(), 
            news.getUpdatedAt(),
            news.getUpdatedBy());
    }
}
