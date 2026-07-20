package com.apteka.portal.dtos.response.apteka;

import java.util.UUID;

import com.apteka.portal.models.Address;

public record AdressResponseDTO(
    String city,
    String street,
    String house,
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
