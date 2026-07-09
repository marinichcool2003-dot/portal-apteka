package com.apteka.portal.dtos.response.client;

import com.apteka.portal.dtos.response.AssignedStatsDTO;

public record ClientWithStatsDTO(
    ClientResponseDTO client,
    AssignedStatsDTO stats
) 
{} 
