package com.apteka.portal.dtos.request.apteka;

import java.util.UUID;

public record AdressRequestDTO(
    String city,
    String street,
    String house,
    UUID fiasId
) {}
