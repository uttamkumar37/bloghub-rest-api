package com.bloghub.api.event;

import java.time.Instant;

public record EmailVerificationRequestedEvent(Long userId, String email, Instant occurredAt) implements BlogEvent {

    @Override
    public String eventType() {
        return "EMAIL_VERIFICATION_REQUESTED";
    }
}
