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

import com.apteka.portal.dtos.response.TaskPictureResponseDTO;
import com.apteka.portal.services.TaskPictureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/task-pictures")
@RequiredArgsConstructor
public class TaskPictureController {
    private final TaskPictureService taskPictureService;

    @PostMapping("/upload-to-task/{taskId}")
    public ResponseEntity<TaskPictureResponseDTO> uploadPicture(
            @PathVariable Long taskId, 
            @RequestParam("file") MultipartFile file) throws IOException{
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskPictureService.uploadPicture(taskId, file));
    }

    @GetMapping("/{pictureId}")
    public ResponseEntity<Resource> getPicture(@PathVariable Long pictureId) {
        File file = taskPictureService.getFileById(pictureId);
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
    }

}
