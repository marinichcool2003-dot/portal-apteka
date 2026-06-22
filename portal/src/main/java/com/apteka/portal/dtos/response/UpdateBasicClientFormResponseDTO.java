package com.apteka.portal.dtos.response;

import com.apteka.portal.models.Client;

public record UpdateBasicClientFormResponseDTO(
    Client client,
    boolean hasChange
) {}
