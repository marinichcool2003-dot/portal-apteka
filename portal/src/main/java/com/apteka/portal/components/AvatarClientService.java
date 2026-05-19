package com.apteka.portal.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AvatarClientService {

    @Value("${app.default.avatars.upload.dir}")
    private String uploadDir;

    private final List<String> allowedContentTypes = List.of("image/png", "image/jpeg");

    private final long maxFileSize = 2 * 1024 * 1024;

    public String uploadAvatar(MultipartFile file, UUID clientId) throws IOException {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = clientId + extension;
        Path path = Paths.get(uploadDir).resolve(fileName).toAbsolutePath();

        Files.createDirectories(path.getParent());

        deleteAvatarIfExists(clientId);

        Files.write(path, file.getBytes());

        return "/avatars/" + fileName;
    }

    public void deleteAvatarIfExists(UUID clientId) {
        try (var files = Files.list(Paths.get(uploadDir).toAbsolutePath())) {
            files.filter(p -> p.getFileName().toString().startsWith(clientId.toString()))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException e) {

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
