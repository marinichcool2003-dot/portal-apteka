package com.apteka.portal.dtos.response;

import com.apteka.portal.models.Account;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с аккаунтом и признаком изменений")
public record AccountHasChangeResponseDTO(
    @Schema(description = "Аккаунт")
    Account account,
    @Schema(description = "Признак наличия изменений")
    boolean hasChange
) {}
