package com.apteka.portal.services;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.response.TaskPictureResponseDTO;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.TaskPictureNotFoundException;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPicture;
import com.apteka.portal.repository.TaskPictureRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskPictureService {
    private final TaskPictureRepository taskPictureRepository;
    private final TaskRepository taskRepository;

    @Value("${app.default.upload.task-picture.dir}")
    private String uploadDir;

    @Transactional
    public TaskPictureResponseDTO uploadPicture(Long taskId, MultipartFile file) throws IOException{
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        File destFolder = new File(uploadDir);

        File targetFile = new File(destFolder, uniqueFileName);
        file.transferTo(targetFile);

        TaskPicture picture = TaskPicture.builder()
            .path(targetFile.getAbsolutePath())
            .task(task)
            .build();

        taskPictureRepository.save(picture);
        return TaskPictureResponseDTO.from(picture);
    }

    @Transactional(readOnly = true)
    public File getFileById(Long pictureId) {
        TaskPicture picture = taskPictureRepository.findById(pictureId)
            .orElseThrow(() -> new TaskPictureNotFoundException(pictureId));
        return new File(picture.getPath()); 
    }
}
