package com.apteka.portal.dtos.request.apteka;

import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;

import jakarta.validation.constraints.Positive;

public record AptekaUpdateDescriptionRequestDTO(
        @Positive Integer number,
        String adress) {
    public static AptekaUpdateDescriptionRequestDTO from(AptekaUpdateRequestDTO dto) {
        return new AptekaUpdateDescriptionRequestDTO(dto.number(), dto.adress());
    }
}
