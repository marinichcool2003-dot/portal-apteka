package com.apteka.portal.dtos.response;

import com.apteka.portal.models.Client;

public record ClientWithStatsDTO(
    Client client,
    TaskStatsDTO stats
) 
{} 
