package com.apteka.portal.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
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
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.TaskPictureService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/task-pictures")
@RequiredArgsConstructor
public class TaskPictureController {

    private final TaskPictureService taskPictureService;

    @PostMapping(value = "/upload-to-task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaskPictureResponseDTO> uploadPicture(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskPictureService.uploadPicture(taskId, file, currentUser));
    }

    @GetMapping("/{pictureId}")
    public ResponseEntity<Resource> getPicture(@PathVariable Long pictureId,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {

        Path file = taskPictureService.getFileById(pictureId, currentUser);
        Resource resource = new FileSystemResource(file);

        String contentType = Files.probeContentType(file);

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }
}