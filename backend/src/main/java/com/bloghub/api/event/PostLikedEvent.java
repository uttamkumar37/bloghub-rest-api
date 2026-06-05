package com.bloghub.api.event;

import java.time.Instant;

public record PostLikedEvent(Long postId, Long userId, Instant occurredAt) implements BlogEvent {

    @Override
    public String eventType() {
        return "POST_LIKED";
    }
}
