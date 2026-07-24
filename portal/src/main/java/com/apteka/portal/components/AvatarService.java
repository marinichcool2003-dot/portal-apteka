package com.apteka.portal.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AvatarService {

    @Value("${app.default.avatars.upload.dir}")
    private String uploadDir;

    private final List<String> allowedContentTypes = List.of("image/png", "image/jpeg");

    private final long maxFileSize = 2 * 1024 * 1024;

    public String uploadClientAvatar(MultipartFile file, UUID clientId) throws IOException {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = clientId + extension;
        Path path = Paths.get(uploadDir).resolve(fileName).toAbsolutePath();

        Files.createDirectories(path.getParent());

        deleteClientAvatarIfExists(clientId);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/avatars/" + fileName;
    }

    public String uploadUserGroupAvatar(MultipartFile file, Integer userGroupId) throws IOException {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = "user_group" + userGroupId + extension;
        Path path = Paths.get(uploadDir).resolve(fileName).toAbsolutePath();

        Files.createDirectories(path.getParent());

        deleteUserGroupAvatarIfExists(userGroupId);

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/avatars/" + fileName;
    }

    public void deleteUserGroupAvatarIfExists(Integer userGroupId) {
        try (var files = Files.list(Paths.get(uploadDir).toAbsolutePath())) {
            files.filter(p -> p.getFileName().toString().startsWith("user_group" + userGroupId.toString()))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete user-group avatar {}", p, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to list user-group avatars for deletion", e);
        }
    }

    public void deleteClientAvatarIfExists(UUID clientId) {
        try (var files = Files.list(Paths.get(uploadDir).toAbsolutePath())) {
            files.filter(p -> p.getFileName().toString().startsWith(clientId.toString()))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete client avatar {}", p, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to list client avatars for deletion", e);
        }
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла. Только PNG и JPEG разрешены.");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Файл слишком большой. Максимальный размер — 2 МБ.");
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
}
