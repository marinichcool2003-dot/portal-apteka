package com.apteka.portal.dtos.response;

import java.util.Optional;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.WorkType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными типа работ")
public record WorkTypeResponseDTO(
        @Schema(description = "Идентификатор")
        Integer id,
        @Schema(description = "Наименование")
        String name,
        @Schema(description = "Ссылка на Wiki")
        String wikiLink,
        @Schema(description = "Приоритет типа работ")
        PriorityResponseDTO priorityResponseDTO,
        @Schema(description = "Краткие данные группы задач")
        TaskGroupShortDTO taskGroup) {
    // AUDIT-FIX: @Schema Swagger RU
    @Schema(description = "Приоритет типа работ")
    public record PriorityResponseDTO(
            @Schema(description = "Код приоритета")
            String priorityCode,
            @Schema(description = "Описание приоритета")
            String priorityDescription) {
    }
    // AUDIT-FIX: @Schema Swagger RU + creator/executor группы категории
    @Schema(description = "Краткие данные группы задач")
    public record TaskGroupShortDTO(
            @Schema(description = "Идентификатор")
            Integer id,
            @Schema(description = "Наименование")
            String name,
            // AUDIT-FIX: сторона создателя категории
            @Schema(description = "Группа-создатель категории")
            UserGroupShortResponseDTO creatorGroup,
            // AUDIT-FIX: сторона исполнителя (intendedGroup)
            @Schema(description = "Группа-исполнитель категории")
            UserGroupShortResponseDTO executorGroup) {
    }

    public static WorkTypeResponseDTO from(WorkType workType) {
        var gt = workType.getGroupTask();
        return new WorkTypeResponseDTO(
                workType.getId(),
                workType.getName(),
                workType.getWikiLink(),
                new PriorityResponseDTO(
                    workType.getPriority().getCode(), 
                    workType.getPriority().getDescription()),
                new TaskGroupShortDTO(
                        gt.getId(),
                        gt.getName(),
                        Optional.ofNullable(gt.getCreatorGroup()).map(UserGroupShortResponseDTO::from).orElse(null),
                        Optional.ofNullable(gt.getIntendedGroup()).map(UserGroupShortResponseDTO::from).orElse(null)));
    }
}
