package com.apteka.portal.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Статус отправки письма")
@Getter
@AllArgsConstructor
public enum MailOutboxStatus {
    @Schema(description = "Ожидает отправки")
    PENDING("PENDING"),
    @Schema(description = "Отправлено")
    SENT("SENT"),
    @Schema(description = "Ошибка отправки (будет повтор)")
    FAILED("FAILED"),
    @Schema(description = "Постоянный отказ, повтор не выполняется")
    REJECTED("REJECTED");

    private final String code;
}
