package com.apteka.portal.dtos.response;

import java.util.Optional;

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
        } else if (taskComments.getAccount().getClient() != null) {
            type = UserType.APTEKA;
            authorName = Optional.ofNullable(taskComments.getAccount().getUserGroup())
                    .map(ug -> ug.getName() + " " + taskComments.getAccount().getApteka().getNumber())
                    .orElse(taskComments.getAccount().getLogin());
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
