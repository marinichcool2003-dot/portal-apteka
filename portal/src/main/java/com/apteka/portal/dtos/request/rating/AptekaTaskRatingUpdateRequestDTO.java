package com.apteka.portal.dtos.request.rating;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление оценки аптеки по задаче")
public record AptekaTaskRatingUpdateRequestDTO(
        @Schema(description = "Оценка от 1 до 5 звёзд", example = "4")
        @NotNull(message = "Оценка обязательна")
        @Min(value = 1, message = "Оценка должна быть от 1 до 5")
        @Max(value = 5, message = "Оценка должна быть от 1 до 5")
        Integer stars,

        @Schema(description = "Причина / комментарий к оценке")
        @NotBlank(message = "Причина оценки обязательна")
        @Size(max = 1000, message = "Причина не может превышать 1000 символов")
        String reason
) {}
