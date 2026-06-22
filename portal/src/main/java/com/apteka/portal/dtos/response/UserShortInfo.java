package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.models.Task;
import com.apteka.portal.models.UserType;

public record UserShortInfo(
        Object id,
        UserType type,
        String displayName) {

    public static UserShortInfo resolveCreator(Task task) {
        if (task.getCreatedByClient() != null) {
            return new UserShortInfo(task.getCreatedByClient().getId(), UserType.CLIENT,
                    task.getCreatedByClient().getFullName());
        } else if (task.getCreatedByApteka() != null) {
            String aptekaName = Optional.ofNullable(task.getCreatedByApteka().getAccount().getUserGroup())
                    .map(ug -> ug.getName() + " " + task.getCreatedByApteka().getNumber())
                    .orElse(task.getCreatedByApteka().getAccount().getLogin());
            return new UserShortInfo(task.getCreatedByApteka().getId(), UserType.APTEKA, aptekaName);
        }
        return null;
    }

    public static UserShortInfo resolveAssignee(Task task) {
        if (task.getAssignedClient() != null) {
            return new UserShortInfo(task.getAssignedClient().getId(), UserType.CLIENT,
                    task.getAssignedClient().getFullName());
        } else if (task.getAssignedApteka() != null) {
            String aptekaName = Optional.ofNullable(task.getAssignedApteka().getAccount().getUserGroup())
                    .map(ug -> ug.getName() + " " + task.getAssignedApteka().getNumber())
                    .orElse(task.getAssignedApteka().getAccount().getLogin());
            return new UserShortInfo(task.getAssignedApteka().getId(), UserType.APTEKA, aptekaName);
        }
        return null;
    }
}
