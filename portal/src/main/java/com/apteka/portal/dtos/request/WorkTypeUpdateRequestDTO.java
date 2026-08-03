package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление типа работ")
public record WorkTypeUpdateRequestDTO(
		@Schema(description = "Наименование")
		String name,
		@Schema(description = "Идентификатор группы задач")
		@Positive(message = "Тип работ должен быть больше нуля!") Integer groupTaskId,
		@Schema(description = "Код приоритета")
		String priorityCode,
		@Schema(description = "Комментарий для создателя задачи")
		@Size(max = 1024, message = "Комментарий слишком длинный!") String commentForCreator,
		@Schema(description = "Ссылка на Wiki")
		@Size(max = 2048, message = "Ссылка слишком длинная!") String wiki_link) {

}
