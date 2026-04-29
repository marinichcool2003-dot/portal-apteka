package com.apteka.portal.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AvatarClientService {

    private final String uploadDir = "/uploads/clients/avatars";

    private final List<String> allowedContentTypes = List.of("image/png", "image/jpeg");

    private final long maxFileSize = 2 * 1024 * 1024;

    public String uploadAvatar(MultipartFile file, UUID clientId) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла. Только PNG и JPEG разрешены.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null ||
            !(originalFileName.toLowerCase().endsWith(".png") || originalFileName.toLowerCase().endsWith(".jpg") || originalFileName.toLowerCase().endsWith(".jpeg"))) {
            throw new IllegalArgumentException("Неподдерживаемое расширение файла. Только .png и .jpg разрешены.");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Файл слишком большой. Максимальный размер — 2 МБ.");
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = clientId + extension;
        Path path = Paths.get(uploadDir + "/" + fileName);

        Files.createDirectories(path.getParent());

        Files.write(path, file.getBytes());

        return "/avatars/" + fileName;
    }
}
