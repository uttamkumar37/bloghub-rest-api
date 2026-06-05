package com.bloghub.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile storePostCover(MultipartFile file);

    record StoredFile(String fileName, String contentType, long sizeBytes, String publicUrl) {
    }
}
