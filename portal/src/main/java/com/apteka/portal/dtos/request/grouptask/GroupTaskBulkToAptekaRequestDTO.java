package com.apteka.portal.dtos.request.grouptask;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Массовое создание категории задач на все видимые группы аптек")
public record GroupTaskBulkToAptekaRequestDTO(
        @Schema(description = "Наименование категории")
        @NotBlank(message = "Наименование группы задач не может быть пустым!")
        String name,
        @Schema(description = "Идентификатор группы-создателя (отдел)")
        @NotNull(message = "Идентификатор группы создателя не может быть пустым!")
        @Positive(message = "Группа создателя должна быть больше нуля!")
        Integer creatorGroupId,
        @Schema(description = "Опциональные виды работ — создаются на каждый GroupTask")
        @Valid
        List<GroupTaskBulkWorkTypeItemDTO> workTypes) {

    @Schema(description = "Вид работ для массового создания")
    public record GroupTaskBulkWorkTypeItemDTO(
            @Schema(description = "Наименование")
            @NotBlank(message = "Наименование типа работ не может быть пустым!")
            String name,
            @Schema(description = "Код приоритета")
            String priorityCode,
            @Schema(description = "Комментарий для создателя задачи")
            @Size(max = 1024, message = "Комментарий слишком длинный!")
            String commentForCreator,
            @Schema(description = "Ссылка на Wiki")
            @Size(max = 2048, message = "Ссылка слишком длинная!")
            String wiki_link) {
    }
}
