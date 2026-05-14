package com.apteka.portal.dtos.response;

import com.apteka.portal.models.TaskPicture;

public record TaskPictureResponseDTO(
        Long id,
        String downloadUrl) {
    public static TaskPictureResponseDTO from(TaskPicture picture) {
        return new TaskPictureResponseDTO(
                picture.getId(),
                "/api/v1/tasks/pictures/download" + picture.getId());
    }
}
