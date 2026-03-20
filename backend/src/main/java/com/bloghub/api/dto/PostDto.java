package com.bloghub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning post data with author info and like/comment counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;

    /** Author's user ID */
    private Long authorId;
    /** Author's display name */
    private String authorName;
    /** Author's username */
    private String authorUsername;

    private int commentsCount;
    private int likesCount;

    /** Whether the currently authenticated user has liked this post */
    private boolean likedByCurrentUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
