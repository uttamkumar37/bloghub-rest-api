# Async Events

## Purpose
Define BlogHub's asynchronous event model for email verification, comment notifications, post liked events, retries, and dead-letter handling.

## Practical Usage Steps
1. Persist business data in the main transaction.
2. Save an outbox event in the same transaction.
3. A publisher job later reads pending events and publishes to Kafka or RabbitMQ.
4. Retry transient failures with backoff.
5. Move poison messages to a dead-letter state.

## Implementation Notes
- Current implementation creates outbox records for registration, new comments, and post likes.
- Event records live in `outbox_events`.
- Broker publishing is intentionally behind the outbox boundary to avoid coupling transactions to network calls.

## Event Types
| Event | Trigger | Consumer |
|---|---|---|
| `EMAIL_VERIFICATION_REQUESTED` | User registration | Email service |
| `NEW_COMMENT_NOTIFICATION` | New comment | Notification service |
| `POST_LIKED` | Like added | Notification/analytics service |

## Failure Modes
- Broker down: outbox rows remain pending.
- Poison payload: event exceeds retry limit and moves to dead letter.
- Duplicate publish: consumers need idempotency by event id.

## Monitoring Notes
- Pending outbox count.
- Oldest pending event age.
- Publish success/failure rate.
- Dead-letter count.

## Rollback Notes
Because events are persisted before publishing, disabling the publisher stops side effects without losing business writes.

## Practical Example
```json
{
  "eventType": "POST_LIKED",
  "aggregateType": "POST",
  "aggregateId": "42",
  "payload": {"postId": 42, "userId": 7}
}
```

## Interview Questions
- Why use outbox instead of publishing directly inside a transaction?
- How do consumers handle duplicate events?
- What belongs in a dead-letter queue?

## Common Mistakes
- Calling Kafka/RabbitMQ before committing the database transaction.
- Not making consumers idempotent.
- No dashboard for pending event age.

## Self-Check
- [ ] Every async side effect has an event type.
- [ ] Retry and DLQ behavior is documented.
- [ ] Main transaction does not depend on broker availability.

