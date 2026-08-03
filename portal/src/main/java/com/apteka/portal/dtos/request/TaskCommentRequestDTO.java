package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
@Schema(description = "Запрос на создание комментария к задаче")
public record TaskCommentRequestDTO(
        @Schema(description = "Текст комментария")
        @NotBlank(message = "Комментарий не может быть пустым")
        String commentText,
        @Schema(description = "Идентификатор задачи")
        @NotNull
        @Positive(message = "Идентификатор задачи должен быть больше нуля")
        Long taskId
) {}
