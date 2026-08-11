package com.apteka.portal.dtos.response.rating;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Постраничный список оценок аптеки со средней оценкой и количеством")
public record AptekaRatingsPageResponseDTO(
        @Schema(description = "Список оценок")
        Page<AptekaTaskRatingResponseDTO> ratings,
        @Schema(description = "Средняя оценка по всем записям аптеки")
        Double averageStars,
        @Schema(description = "Общее количество оценок аптеки")
        Long totalCount
) {}
