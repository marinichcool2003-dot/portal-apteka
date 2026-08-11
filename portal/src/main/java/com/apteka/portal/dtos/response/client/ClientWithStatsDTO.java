package com.apteka.portal.dtos.response.client;

import com.apteka.portal.dtos.response.AssignedStatsDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Клиент со статистикой задач")
public record ClientWithStatsDTO(
    @Schema(description = "Данные клиента")
    ClientResponseDTO client,
    @Schema(description = "Статистика задач")
    AssignedStatsDTO stats
) 
{} 
