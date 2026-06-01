package com.apteka.portal.dtos.response;

import java.util.UUID;

public record CreatedStatsDTO(
    UUID clientId,
    Long openCreated
) {}
