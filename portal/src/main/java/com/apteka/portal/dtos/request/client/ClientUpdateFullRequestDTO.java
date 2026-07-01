package com.apteka.portal.dtos.request.client;

import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record ClientUpdateFullRequestDTO(
    AccountUpdateRequestDTO accountUpdateRequestDTO,
    ClientUpdateDescriptionRequestDTO clientUpdateRequestDTO,
    @Schema(description = "Аватар пользователя", type = "string", format = "binary") MultipartFile avatar,
    @Positive(message = "Идентификатор группы должен быть больше нуля!") Integer userGroupId
) {}
