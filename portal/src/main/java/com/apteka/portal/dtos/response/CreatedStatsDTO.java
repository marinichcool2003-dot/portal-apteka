package com.apteka.portal.dtos.response;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статистика созданных задач")
public record CreatedStatsDTO(
    @Schema(description = "Идентификатор клиента")
    UUID clientId,
    @Schema(description = "Количество созданных открытых задач")
    Long openCreated
) {}
