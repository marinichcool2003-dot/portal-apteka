package com.apteka.portal.dtos.response;

import com.apteka.portal.models.GroupTask;

public record GroupTaskResponseDTO(
    Integer id,
    String name,
    UserGroupShortInfo userGroup
) {
    public record UserGroupShortInfo(Integer id, String name) {}

    public static GroupTaskResponseDTO from(GroupTask groupTask) {
        return new GroupTaskResponseDTO(
            groupTask.getId(), 
            groupTask.getName(), 
            new UserGroupShortInfo(
                groupTask.getUserGroup().getId(),
                groupTask.getUserGroup().getName()
            )
        );
    }
}
