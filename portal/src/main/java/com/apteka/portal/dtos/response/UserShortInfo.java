package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.models.Task;
import com.apteka.portal.models.UserType;

public record UserShortInfo(
        Object id,
        UserType type,
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
                return new UserShortInfo(task.getCreator().getId(), UserType.CLIENT, displayName);
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
                return new UserShortInfo(task.getAssigner().getId(), UserType.CLIENT, displayName);
            }
        }
        return null;
    }
}
