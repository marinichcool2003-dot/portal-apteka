package com.apteka.portal.dtos.request;

public record TaskUpdateRequestDTO(
    String title,
    String description,
    String comments
)
{}
