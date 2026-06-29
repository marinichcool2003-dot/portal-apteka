package com.apteka.portal.dtos.response;

import com.apteka.portal.models.Account;

public record AccountHasChangeResponseDTO(
    Account account,
    boolean hasChange
) {}
