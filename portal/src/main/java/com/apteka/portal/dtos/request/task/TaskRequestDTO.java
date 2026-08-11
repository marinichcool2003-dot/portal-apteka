package com.apteka.portal.dtos.request.task;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Контракт данных задачи")
public interface TaskRequestDTO {
    @Schema(description = "Заголовок задачи")
    public String title();
    @Schema(description = "Описание задачи")
    public String description();
    @Schema(description = "Идентификатор типа работ")
    public Integer workTypeId();
    @Schema(description = "Идентификатор исполнителя задачи")
    public UUID assignerId();
}
