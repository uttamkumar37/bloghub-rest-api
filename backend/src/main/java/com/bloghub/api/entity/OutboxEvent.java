package com.bloghub.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_status_next_attempt", columnList = "status, next_attempt_at"),
    @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 80)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public enum Status {
        PENDING,
        PUBLISHED,
        FAILED,
        DEAD_LETTER
    }
}
