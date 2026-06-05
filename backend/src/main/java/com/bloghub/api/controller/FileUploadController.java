package com.bloghub.api.controller;

import com.bloghub.api.dto.ApiResponse;
import com.bloghub.api.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Authenticated image upload APIs")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/post-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a post cover image", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> uploadPostCover(@RequestPart("file") MultipartFile file) {
        FileStorageService.StoredFile stored = fileStorageService.storePostCover(file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", stored));
    }
}
