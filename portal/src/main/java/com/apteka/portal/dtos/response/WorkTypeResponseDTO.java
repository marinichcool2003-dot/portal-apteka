package com.apteka.portal.dtos.response;

import com.apteka.portal.models.WorkType;

public record WorkTypeResponseDTO(
        Integer id,
        String name,
        TaskGroupShortDTO taskGroup) {
    public record TaskGroupShortDTO(Integer id, String name) {
    }

    public static WorkTypeResponseDTO from(WorkType workType) {
        return new WorkTypeResponseDTO(
                workType.getId(),
                workType.getName(),
                new TaskGroupShortDTO(
                        workType.getGroupTask().getId(),
                        workType.getGroupTask().getName()));
    }
}
