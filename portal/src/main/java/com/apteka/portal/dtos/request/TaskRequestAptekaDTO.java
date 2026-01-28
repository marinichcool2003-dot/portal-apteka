package com.apteka.portal.dtos.request;

public record TaskRequestAptekaDTO(
    String title,
    String description,
    String comments,
    Integer aptekaId,
    Integer workTaskId
) 
{}
