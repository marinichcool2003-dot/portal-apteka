package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.models.Task;
import com.apteka.portal.models.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о пользователе")
public record UserShortInfo(
        @Schema(description = "Идентификатор")
        Object id,
        @Schema(description = "Тип пользователя")
        UserType type,
        @Schema(description = "Отображаемое имя")
        String displayName) {

    public static UserShortInfo resolveCreator(Task task) {

        if (task.getCreator() != null) {
            if (task.getCreator().getClient() != null) {
                String displayName = task.getCreator().getClient().getFullName();
                return new UserShortInfo(task.getCreator().getId(), UserType.CLIENT, displayName);
            }
            else if (task.getCreator().getApteka() != null) {
                String displayName = Optional.ofNullable(task.getCreator().getUserGroup())
                        .map(ug -> ug.getName() + " " + task.getCreator().getApteka().getNumber())
                        .orElse(task.getCreator().getLogin());
                // AUDIT-FIX: P-11 — Preserve the pharmacy account type in short task identities.
                return new UserShortInfo(task.getCreator().getId(), UserType.APTEKA, displayName);
            }
        }
        return null;
    }

    public static UserShortInfo resolveAssignee(Task task) {
        if (task.getAssigner() != null) {
            if (task.getAssigner().getClient() != null) {
                String displayName = task.getAssigner().getClient().getFullName();
                return new UserShortInfo(task.getAssigner().getId(), UserType.CLIENT, displayName);
            }
            else if (task.getAssigner().getApteka() != null) {
                String displayName = Optional.ofNullable(task.getAssigner().getUserGroup())
                        .map(ug -> ug.getName() + " " + task.getAssigner().getApteka().getNumber())
                        .orElse(task.getAssigner().getLogin());
                // AUDIT-FIX: P-11 — Preserve the pharmacy account type in short task identities.
                return new UserShortInfo(task.getAssigner().getId(), UserType.APTEKA, displayName);
            }
        }
        return null;
    }
}
