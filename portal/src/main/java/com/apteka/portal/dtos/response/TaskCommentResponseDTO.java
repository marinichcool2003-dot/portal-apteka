package com.apteka.portal.dtos.response;

import com.apteka.portal.models.TaskComment;
import com.apteka.portal.models.UserType;

public record TaskCommentResponseDTO(
        Long id,
        String comment,
        UserType authorType,
        String authorName,
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
