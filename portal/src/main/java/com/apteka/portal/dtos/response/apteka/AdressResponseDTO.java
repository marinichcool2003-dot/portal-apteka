package com.apteka.portal.dtos.response.apteka;

import java.util.UUID;

import com.apteka.portal.models.Address;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными адреса")
public record AdressResponseDTO(
    @Schema(description = "Город")
    String city,
    @Schema(description = "Улица")
    String street,
    @Schema(description = "Дом")
    String house,
    @Schema(description = "Идентификатор адреса ФИАС")
    UUID fiasId
) {
    public static AdressResponseDTO from(Address adress) {
        return new AdressResponseDTO(
            adress.getCity(),
            adress.getStreet(),
            adress.getHouse(),
            adress.getFiasId());
    }
}
