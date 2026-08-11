package com.apteka.portal.dtos.response;

import com.apteka.portal.models.TaskComment;
import com.apteka.portal.models.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с комментарием к задаче")
public record TaskCommentResponseDTO(
        @Schema(description = "Идентификатор")
        Long id,
        @Schema(description = "Текст комментария")
        String comment,
        @Schema(description = "Тип автора")
        UserType authorType,
        @Schema(description = "Имя автора")
        String authorName,
        @Schema(description = "Идентификатор автора")
        Object authorId) {
    public static TaskCommentResponseDTO from(TaskComment taskComments) {
        UserType type = null;
        String authorName = "Система";
        Object authorId = null;

        if (taskComments.getAccount().getClient() != null) {
            type = UserType.CLIENT;
            authorName = taskComments.getAccount().getClient().getFullName();
            authorId = taskComments.getAccount().getId();
        } else if (taskComments.getAccount().getApteka() != null) {
            type = UserType.APTEKA;
            authorName = taskComments.getAccount().getApteka().getAptekaName();
            authorId = taskComments.getAccount().getId();
        }
        return new TaskCommentResponseDTO(
                taskComments.getId(),
                taskComments.getComment(),
                type,
                authorName,
                authorId);
    }
}
