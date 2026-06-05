package com.bloghub.api.service.outbox;

import com.bloghub.api.entity.OutboxEvent;
import com.bloghub.api.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Override
    public OutboxEvent saveEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(OutboxEvent.Status.PENDING)
                .nextAttemptAt(LocalDateTime.now())
                .build();
        return outboxEventRepository.save(event);
    }
}
