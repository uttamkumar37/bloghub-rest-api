package com.bloghub.api.service.outbox;

import com.bloghub.api.entity.OutboxEvent;

public interface OutboxService {

    OutboxEvent saveEvent(String aggregateType, String aggregateId, String eventType, String payload);
}
