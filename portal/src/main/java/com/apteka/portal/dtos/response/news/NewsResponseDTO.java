package com.apteka.portal.dtos.response.news;

import java.time.Instant;
import java.util.Optional;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.News;
import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
@Schema(description = "Ответ с данными новости")
public record NewsResponseDTO(
    @Schema(description = "Идентификатор")
    Integer id,
    @Schema(description = "Заголовок")
    String title,
    @Schema(description = "Текст новости")
    String newsText,
    @Schema(description = "Имя автора")
    String authorName,
    @Schema(description = "Группа пользователей")
    UserGroupShortResponseDTO userGroup,
    @Schema(description = "Дата создания")
    Instant creationDate,
    @Schema(description = "Дата обновления")
    Instant updatedAt,
    @Schema(description = "Кем обновлено")
    String updatedBy
) {
    public static NewsResponseDTO from(News news) {
        return new NewsResponseDTO(
            news.getId(), 
            news.getTitle(), 
            news.getNewsText(),
            Optional.ofNullable(news.getAuthor()).map(author -> author.getFullName()).orElse(null),
            Optional.ofNullable(news.getUserGroup()).map(UserGroupShortResponseDTO::from).orElse(null),
            news.getCreationDate(), 
            news.getUpdatedAt(),
            news.getUpdatedBy());
    }
}
