package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.models.TaskComment;
import com.apteka.portal.models.UserType;

public record TaskCommentResponseDTO(
    Long id,
    String comment,
    UserType authorType,
    String authorName,
    Object authorId
) {
    public static TaskCommentResponseDTO from(TaskComment taskComments) {
        UserType type = null;
        String authorName = "Система";
        Object authorId = null;

        if (taskComments.getClient() != null) {
            type = UserType.CLIENT;
            authorName = taskComments.getClient().getFullName();
            authorId = taskComments.getClient().getId();
        } else if (taskComments.getApteka() != null) {
            type = UserType.APTEKA;
            authorName = Optional.ofNullable(taskComments.getApteka().getUserGroup())
                .map(group -> group.getName() + " " + taskComments.getApteka().getNumber())
                .orElse(taskComments.getApteka().getLogin());
            authorId = taskComments.getApteka().getId();
        }

        return new TaskCommentResponseDTO(
            taskComments.getId(), 
            taskComments.getComment(), 
            type, 
            authorName, 
            authorId
        );
    }
}
