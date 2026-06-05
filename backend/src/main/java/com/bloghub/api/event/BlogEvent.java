package com.bloghub.api.event;

import java.time.Instant;

public interface BlogEvent {

    String eventType();

    Instant occurredAt();
}
