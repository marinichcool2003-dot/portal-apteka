package com.apteka.portal.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.components.SseAfterCommitPublisher;
import com.apteka.portal.components.servicesecurity.TaskSecurityService;
import com.apteka.portal.dtos.response.TaskPictureResponseDTO;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.TaskPictureNotFoundException;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPicture;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.repository.TaskPictureRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskPictureService {
    private final TaskPictureRepository taskPictureRepository;
    private final TaskRepository taskRepository;
    private final TaskSecurityService taskSecurityService;
    private final SseAfterCommitPublisher sseAfterCommitPublisher;

    @Value("${app.default.upload.task-picture.dir}")
    private String uploadDir;

    @Transactional
    public TaskPictureResponseDTO uploadPicture(Long taskId, MultipartFile file, AppUserDetails currentUser) throws IOException {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
        taskSecurityService.canSelectTask(task, currentUser);
        String extension = validateAndGetExtension(file);
        Path uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
        Path targetPath = uploadRoot.resolve(UUID.randomUUID() + extension).normalize();
        if (!targetPath.startsWith(uploadRoot)) {
            throw new AccessDeniedException("Недопустимый путь загружаемого файла");
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        TaskPicture picture = TaskPicture.builder()
            .path(targetPath.toString())
            .task(task)
            .build();

        taskPictureRepository.save(picture);
        var signal = new SseEventNames.EntityUpdateSignalDTO(picture.getTask().getId(), SseSignalTypes.UPDATED);
        sseAfterCommitPublisher.publishAfterCommit(SseEventNames.REFRESH_TASKS, signal);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try {
                        Files.deleteIfExists(targetPath);
                    } catch (IOException ignored) {
                        // The failed cleanup cannot change the completed transaction outcome.
                    }
                }
            }
        });

        return TaskPictureResponseDTO.from(picture);
    }

    @Transactional(readOnly = true)
    public Path getFileById(Long pictureId, AppUserDetails currentUser) {
        TaskPicture picture = taskPictureRepository.findById(pictureId)
            .orElseThrow(() -> new TaskPictureNotFoundException(pictureId));
        taskSecurityService.canSelectTask(picture.getTask(), currentUser);
        Path uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        Path picturePath = Path.of(picture.getPath()).toAbsolutePath().normalize();
        if (!picturePath.startsWith(uploadRoot)) {
            throw new AccessDeniedException("Недопустимый путь изображения задачи");
        }
        return picturePath;
    }

    private String validateAndGetExtension(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Файл должен быть изображением размером не более 10 МБ");
        }
        Map<String, String> allowedExtensions = Map.of(
                MediaType.IMAGE_PNG_VALUE, ".png",
                MediaType.IMAGE_JPEG_VALUE, ".jpg");
        String extension = allowedExtensions.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Поддерживаются только изображения PNG и JPEG");
        }
        return extension;
    }
}
