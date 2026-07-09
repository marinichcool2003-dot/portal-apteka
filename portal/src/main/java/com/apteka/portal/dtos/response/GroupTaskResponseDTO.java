package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;

public record GroupTaskResponseDTO(
        Integer id,
        String name,
        UserGroupShortResponseDTO creatorGroup,
        UserGroupShortResponseDTO executorGroup) {

    public static GroupTaskResponseDTO from(GroupTask groupTask) {
        return new GroupTaskResponseDTO(
            groupTask.getId(),
            groupTask.getName(), 
            new UserGroupShortResponseDTO(
                Optional.ofNullable(groupTask.getCreatorGroup()).map(UserGroup::getId).orElse(null),
                Optional.ofNullable(groupTask.getCreatorGroup()).map(UserGroup::getName).orElse(null)
            ),
            new UserGroupShortResponseDTO(
                Optional.ofNullable(groupTask.getExecutorGroup()).map(UserGroup::getId).orElse(null),
                Optional.ofNullable(groupTask.getExecutorGroup()).map(UserGroup::getName).orElse(null)
            )
        );
    }
}
