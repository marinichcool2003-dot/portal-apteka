package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.GroupTask;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными группы задач")
public record GroupTaskResponseDTO(
        @Schema(description = "Идентификатор")
        Integer id,
        @Schema(description = "Наименование")
        String name,
        @Schema(description = "Группа-создатель")
        UserGroupShortResponseDTO creatorGroup,
        @Schema(description = "Группа-исполнитель")
        UserGroupShortResponseDTO executorGroup) {

    public static GroupTaskResponseDTO from(GroupTask groupTask) {
        return new GroupTaskResponseDTO(
            groupTask.getId(),
            groupTask.getName(),
            Optional.ofNullable(groupTask.getCreatorGroup()).map(UserGroupShortResponseDTO::from).orElse(null),
            Optional.ofNullable(groupTask.getIntendedGroup()).map(UserGroupShortResponseDTO::from).orElse(null)
        );
    }
}
