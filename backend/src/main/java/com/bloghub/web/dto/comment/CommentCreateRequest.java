package com.bloghub.web.dto.comment;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {
    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

