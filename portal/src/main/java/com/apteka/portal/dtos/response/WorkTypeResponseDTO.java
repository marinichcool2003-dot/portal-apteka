package com.apteka.portal.dtos.response;

import com.apteka.portal.models.WorkType;
import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
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
    // AUDIT-FIX: @Schema Swagger RU
    @Schema(description = "Краткие данные группы задач")
    public record TaskGroupShortDTO(
            @Schema(description = "Идентификатор")
            Integer id,
            @Schema(description = "Наименование")
            String name) {
    }

    public static WorkTypeResponseDTO from(WorkType workType) {
        return new WorkTypeResponseDTO(
                workType.getId(),
                workType.getName(),
                workType.getWikiLink(),
                new PriorityResponseDTO(
                    workType.getPriority().getCode(), 
                    workType.getPriority().getDescription()),
                new TaskGroupShortDTO(
                        workType.getGroupTask().getId(),
                        workType.getGroupTask().getName()));
    }
}