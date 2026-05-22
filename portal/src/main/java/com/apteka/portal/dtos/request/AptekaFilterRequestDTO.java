package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Positive;

public record AptekaFilterRequestDTO(
        String login,
        @Positive(message = "Номер группы может быть только положительным числом") Integer groupId,
        @Positive(message = "Номер аптеки может быть только положительным числом") Integer number,
        String phoneNumber) {
}
