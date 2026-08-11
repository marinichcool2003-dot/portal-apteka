package com.apteka.portal.dtos.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат массового создания категорий задач на группы аптек")
public record GroupTaskBulkToAptekaResponseDTO(
        @Schema(description = "Наименование категории")
        String name,
        @Schema(description = "Группа-создатель (отдел)")
        Integer creatorGroupId,
        @Schema(description = "Сколько создано")
        int createdCount,
        @Schema(description = "Сколько пропущено (уже существовали)")
        int skippedCount,
        @Schema(description = "Детали по каждой группе аптек")
        List<GroupTaskBulkItemResultDTO> items) {

    @Schema(description = "Результат по одной intended-группе")
    public record GroupTaskBulkItemResultDTO(
            Integer intendedGroupId,
            String intendedGroupName,
            Integer groupTaskId,
            @Schema(description = "CREATED или SKIPPED")
            String status) {
    }
}
