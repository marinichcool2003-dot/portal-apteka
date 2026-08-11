package com.apteka.portal.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статистика задач подразделения")
public record DepartmentTaskStatsDTO(
    @Schema(description = "Идентификатор группы пользователей")
    Integer groupId,
    @Schema(description = "Наименование группы")
    String groupName,
    @Schema(description = "Количество открытых задач")
    long openTasks,
    @Schema(description = "Количество завершённых задач")
    long completedTasks,
    @Schema(description = "Количество отклонённых задач")
    long deniedTasks,
    @Schema(description = "Общее количество задач")
    long totalTasks
) {}
