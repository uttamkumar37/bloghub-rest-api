# Outbox Pattern

## Purpose
Explain the reliability pattern used to bridge database transactions and asynchronous messaging.

## Practical Usage Steps
1. Save domain change and outbox event in one DB transaction.
2. Commit once.
3. Publisher reads `PENDING` events.
4. Publish to broker.
5. Mark `PUBLISHED`, or increment attempts and schedule retry.

## Implementation Notes
- `OutboxEvent` contains aggregate, event type, payload, status, attempts, and next attempt time.
- `OutboxService.saveEvent(...)` is used by auth, comments, and likes.
- Future publisher can use `SELECT ... FOR UPDATE SKIP LOCKED` for concurrent workers.

## Failure Modes
- DB commit fails: no business data and no event.
- Broker publish fails: event stays pending for retry.
- Publisher crashes after publish before marking success: duplicate publish is possible; consumers must be idempotent.

## Monitoring Notes
- Alert if oldest pending event age exceeds 5 minutes.
- Track retry count and dead-letter volume.
- Dashboard publish latency from `created_at` to `PUBLISHED`.

## Rollback Notes
Disable publisher worker while leaving event creation enabled. Re-enable after the broker or consumer is healthy.

## Practical Example
```sql
SELECT *
FROM outbox_events
WHERE status = 'PENDING' AND next_attempt_at <= NOW()
ORDER BY created_at
LIMIT 100;
```

## Interview Questions
- What problem does outbox solve?
- Why can outbox still create duplicate messages?
- How would you scale multiple publisher workers?

## Common Mistakes
- Treating outbox as exactly-once messaging.
- Not cleaning old published rows.
- No idempotency key on consumer side.

## Self-Check
- [ ] Domain write and event write share one transaction.
- [ ] Publisher failure does not break user request after commit.
- [ ] Duplicate delivery is expected and handled.

