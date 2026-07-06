package com.apteka.portal.dtos.request.apteka;

import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;

import jakarta.validation.constraints.Positive;

public record AptekaUpdateDescriptionRequestDTO(
        @Positive Integer number,
        AdressRequestDTO adressRequestDTO) {
    public static AptekaUpdateDescriptionRequestDTO from(AptekaUpdateRequestDTO dto) {
        return new AptekaUpdateDescriptionRequestDTO(dto.number(), dto.adressRequestDTO());
    }
}
