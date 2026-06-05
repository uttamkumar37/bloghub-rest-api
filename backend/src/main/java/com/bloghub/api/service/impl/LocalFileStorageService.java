package com.bloghub.api.service.impl;

import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload.local-dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.public-base-url:/uploads}")
    private String publicBaseUrl;

    @Value("${app.upload.max-size-bytes:5242880}")
    private long maxSizeBytes;

    @Override
    public StoredFile storePostCover(MultipartFile file) {
        validate(file);

        String extension = extension(file.getOriginalFilename(), file.getContentType());
        String safeName = UUID.randomUUID() + extension;

        try {
            Path root = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
            Path target = root.resolve(safeName).normalize();
            if (!target.startsWith(root)) {
                throw new BlogApiException(HttpStatus.BAD_REQUEST, "Invalid upload filename");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(safeName, file.getContentType(), file.getSize(), publicBaseUrl + "/" + safeName);
        } catch (IOException ex) {
            log.error("Failed to store upload: {}", ex.getMessage(), ex);
            throw new BlogApiException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BlogApiException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BlogApiException(HttpStatus.BAD_REQUEST, "File exceeds maximum allowed size");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BlogApiException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG, and WebP images are allowed");
        }
    }

    private String extension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (Set.of(".jpg", ".jpeg", ".png", ".webp").contains(ext)) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
