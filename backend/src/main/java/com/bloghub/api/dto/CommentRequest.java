package com.bloghub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating or updating a comment.
 */
@Data
public class CommentRequest {

    @NotBlank(message = "Comment body is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String body;
}
