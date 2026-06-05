# Trade-offs

## Purpose
Record engineering trade-offs so the project reads like deliberate backend design rather than feature accumulation.

## Practical Usage Steps
1. Use this file before interviews and README updates.
2. For each decision, explain why it fits current scale.
3. Name the trigger that would change the decision.

## Implementation Notes
| Decision | Chosen | Alternative | Why |
|---|---|---|---|
| Backend shape | Modular monolith | Microservices | Lower operational overhead for portfolio scope |
| DB | MySQL | Postgres | Existing project uses MySQL; Flyway keeps migration discipline |
| Auth | JWT + refresh rotation | Session cookies | Fits REST API/mobile clients |
| Cache | Redis | In-memory only | Needed for blacklist/rate limit across instances |
| Async | Outbox first | Direct Kafka publish | Preserves transaction reliability |
| Uploads | Local now, S3-compatible design | DB BLOBs | Simpler dev, production-ready direction |

## Failure Modes
- Modular monolith can grow too coupled.
- JWT logout requires blacklist infrastructure.
- Redis fallback is not cluster-wide.
- Local file storage is not production durable.

## Monitoring Notes
Use metrics to decide when trade-offs need revisiting: feed latency, Redis hit ratio, DB CPU, outbox lag, upload volume.

## Rollback Notes
Keep abstractions small so Redis, broker, and storage implementations can be swapped without rewriting controllers.

## Practical Example
Outbox is selected because a notification failure should not leave a comment half-created or publish an event for a rolled-back comment.

## Interview Questions
- Why not microservices?
- Why use Redis for blacklist?
- When would you move uploads to S3?

## Common Mistakes
- Presenting every trade-off as universally best.
- No migration trigger.
- Ignoring operational cost.

## Self-Check
- [ ] Each decision has an alternative.
- [ ] Each alternative has a trigger.
- [ ] Trade-offs are tied to current requirements.

