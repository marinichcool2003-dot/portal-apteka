package com.apteka.portal.dtos.response;

import com.apteka.portal.models.GroupTask;

public record GroupTaskResponseDTO(
        Integer id,
        String name,
        UserGroupShortResponseDTO userGroup) {

    public static GroupTaskResponseDTO from(GroupTask groupTask) {
        return new GroupTaskResponseDTO(
                groupTask.getId(),
                groupTask.getName(),
                new UserGroupShortResponseDTO(
                        groupTask.getUserGroup().getId(),
                        groupTask.getUserGroup().getName()));
    }
}
