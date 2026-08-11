package com.apteka.portal.dtos.request.apteka;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос с данными адреса")
public record AdressRequestDTO(
        @Schema(description = "Город")
        String city,
        @Schema(description = "Улица")
        String street,
        @Schema(description = "Дом")
        String house,
        @Schema(description = "Идентификатор адреса ФИАС")
        UUID fiasId
) {}
