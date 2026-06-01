package com.apteka.portal.dtos.response;

public record ClientWithStatsDTO(
    ClientResponseDTO client,
    AssignedStatsDTO stats
) 
{} 
