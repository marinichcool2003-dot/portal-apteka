package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

public record FullClientUpdateRequestDTO(    
    String login,
    String password,
    MultipartFile avatar,
    String fullName,
    Integer groupClientId
) 
{}
