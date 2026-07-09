package com.apteka.portal.dtos.request.grouptask;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public record GroupTaskUpdateRequestDTO(
        @NotEmpty(message = "Наименование группы задач не может быть пустым!") String name,

        @Positive(message = "Группа создателя должна быть больше нуля!") Integer creatorGroupId,

        @Positive(message = "Группа создателя должна быть больше нуля!") Integer executorGroupId) {
}
