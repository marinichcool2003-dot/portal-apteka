package com.apteka.portal.dtos.response;

import com.apteka.portal.models.TaskPicture;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с изображением задачи")
public record TaskPictureResponseDTO(
        @Schema(description = "Идентификатор")
        Long id,
        @Schema(description = "URL для скачивания")
        String downloadUrl) {
    public static TaskPictureResponseDTO from(TaskPicture picture) {
        return new TaskPictureResponseDTO(
                picture.getId(),
                "/api/v1/tasks/pictures/download" + picture.getId());
    }
}
