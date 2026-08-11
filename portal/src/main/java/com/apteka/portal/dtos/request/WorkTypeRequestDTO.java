package com.apteka.portal.dtos.request;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание типа работ")
public record WorkTypeRequestDTO(
        @Schema(description = "Наименование")
        @NotBlank(message = "Наименование типа работ не может быть пустым!")
        String name,
        @Schema(description = "Идентификатор группы задач")
        @NotNull(message = "Тип работ не может быть пустым!")
        @Positive(message = "Тип работ должен быть больше нуля!")
        Integer groupTaskId,
        @Schema(description = "Код приоритета")
        String priorityCode,
        @Schema(description = "Комментарий для создателя задачи")
        @Size(max = 1024, message = "Комментарий слишком длинный!")
        String commentForCreator,
        @Schema(description = "Ссылка на Wiki")
        @URL(protocol = "https", host = "wiki.farmp.ru", message = "Некорректный формат ссылки! Ссылка должна вести только на wiki.farmp.ru и использовать https")
        @Size(max = 2048, message = "Ссылка слишком длинная!")
        String wiki_link
) {}
