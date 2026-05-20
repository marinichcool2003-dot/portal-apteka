package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

public record FullClientUpdateRequestDTO(
    @NotBlank    
    String login,
    @NotBlank 
    String password,
    @NotBlank 
    MultipartFile avatar,
    @NotBlank 
    String fullName,
    @NotBlank 
    Integer groupClientId
) 
{}
