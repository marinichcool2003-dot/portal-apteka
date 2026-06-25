package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Positive;

public record AptekaUpdateDescriptionRequestDTO(
        @Positive Integer number,
        String adress) {
    public static AptekaUpdateDescriptionRequestDTO from(AptekaUpdateRequestDTO dto) {
        return new AptekaUpdateDescriptionRequestDTO(dto.number(), dto.adress());
    }
}
