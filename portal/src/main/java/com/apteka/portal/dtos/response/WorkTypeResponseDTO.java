package com.apteka.portal.dtos.response;

import com.apteka.portal.models.WorkType;

public record WorkTypeResponseDTO(
        Integer id,
        String name,
        String wikiLink,
        PriorityResponseDTO priorityResponseDTO,
        TaskGroupShortDTO taskGroup) {
    public record PriorityResponseDTO(String priorityCode, String priorityDescription) {
    }
    public record TaskGroupShortDTO(Integer id, String name) {
    }

    public static WorkTypeResponseDTO from(WorkType workType) {
        return new WorkTypeResponseDTO(
                workType.getId(),
                workType.getName(),
                workType.getWikiLink(),
                new PriorityResponseDTO(
                    workType.getPriority().getCode(), 
                    workType.getPriority().getDescription()),
                new TaskGroupShortDTO(
                        workType.getGroupTask().getId(),
                        workType.getGroupTask().getName()));
    }
}