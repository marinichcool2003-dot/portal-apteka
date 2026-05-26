package com.apteka.portal.controllers;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.docs.taskpicture.TaskPictureCreateOperation;
import com.apteka.portal.docs.taskpicture.TaskPictureGetOperation;
import com.apteka.portal.docs.taskpicture.TaskPictureNotFoundResponse;
import com.apteka.portal.docs.taskpicture.TaskPictureValidationErrorResponse;
import com.apteka.portal.dtos.response.TaskPictureResponseDTO;
import com.apteka.portal.services.TaskPictureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/task-pictures")
@RequiredArgsConstructor
@Tag(name = "Картинки к задаче")
public class TaskPictureController {

    private final TaskPictureService taskPictureService;

    @Operation(summary = "Загрузка картинки")
    @PostMapping(value = "/upload-to-task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @TaskPictureCreateOperation
    @TaskPictureValidationErrorResponse
    @TaskPictureNotFoundResponse
    public ResponseEntity<TaskPictureResponseDTO> uploadPicture(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskPictureService.uploadPicture(taskId, file));
    }

    @Operation(summary = "Получение картинки")
    @GetMapping("/{pictureId}")
    @TaskPictureGetOperation
    @TaskPictureNotFoundResponse
    public ResponseEntity<Resource> getPicture(@PathVariable Long pictureId) {

        File file = taskPictureService.getFileById(pictureId);

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}