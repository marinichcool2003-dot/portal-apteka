package com.apteka.portal.dtos.request.client;

import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;

import jakarta.validation.constraints.Positive;

public record ClientUpdateFullRequestDTO(
    AccountUpdateRequestDTO accountUpdateRequestDTO,
    ClientUpdateDescriptionRequestDTO clientUpdateRequestDTO,
    MultipartFile avatar,
    @Positive(message = "Идентификатор группы должен быть больше нуля!") Integer userGroupId
) {}
