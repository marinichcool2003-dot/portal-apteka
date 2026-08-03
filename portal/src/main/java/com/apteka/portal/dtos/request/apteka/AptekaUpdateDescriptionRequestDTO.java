package com.apteka.portal.dtos.request.apteka;

import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;

import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление описания аптеки")
public record AptekaUpdateDescriptionRequestDTO(
        @Schema(description = "Номер аптеки")
        @Positive Integer number,
        @Schema(description = "Адрес аптеки")
        AdressRequestDTO adressRequestDTO) {
    public static AptekaUpdateDescriptionRequestDTO from(AptekaUpdateRequestDTO dto) {
        return new AptekaUpdateDescriptionRequestDTO(dto.number(), dto.adressRequestDTO());
    }
}
