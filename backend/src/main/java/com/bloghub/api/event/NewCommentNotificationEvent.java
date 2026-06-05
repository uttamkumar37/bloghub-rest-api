package com.bloghub.api.event;

import java.time.Instant;

public record NewCommentNotificationEvent(Long postId, Long commentId, Long authorId, Instant occurredAt)
        implements BlogEvent {

    @Override
    public String eventType() {
        return "NEW_COMMENT_NOTIFICATION";
    }
}
