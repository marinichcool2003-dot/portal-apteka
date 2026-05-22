package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Positive;

public record FullClientUpdateRequestDTO(    
    String login,
    String password,
    MultipartFile avatar,
    String fullName,
    @Positive
    Integer groupClientId
) 
{}
