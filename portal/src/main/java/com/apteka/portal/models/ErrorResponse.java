package com.apteka.portal.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Стандартный ответ ошибки")
public class ErrorResponse {
    @Schema(example = "000", description = "HTTP статус ошибки")
    private int status;
    @Schema(example = "Наименование ошибки", description = "Описание ошибки")
    private String errorMessage;
    @Schema(example = "1716282000000", description = "Timestamp ошибки")
    private long currentTimesMillis;
}
