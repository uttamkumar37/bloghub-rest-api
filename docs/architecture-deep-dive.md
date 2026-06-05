# Architecture Deep Dive

## Purpose
Explain BlogHub as a backend portfolio system: API, security, persistence, caching, async events, observability, and deployment.

## Practical Usage Steps
1. Read this before interviews.
2. Walk through one request from frontend to database.
3. Explain two scale bottlenecks and two failure modes.
4. Link decisions to code and docs.

## Implementation Notes
- Spring Boot REST API under `/api/v1`.
- MySQL is the source of truth.
- Redis supports blacklist, rate limiting, cache, counters, and idempotency.
- Flyway owns schema versioning.
- Outbox prepares reliable async messaging.
- Actuator, Prometheus, JSON logs, and request IDs support operations.

## Architecture Diagram
```text
React/Nginx
   |
   v
Spring Boot API -> Spring Security/JWT -> Services -> JPA Repositories -> MySQL
       |                 |                  |
       |                 |                  +-> Outbox events
       |                 +-> Redis blacklist/rate/idempotency/cache
       +-> Actuator/Prometheus + JSON logs + X-Request-ID
       +-> Local uploads / S3-compatible storage design
```

## Failure Modes
- Redis down: graceful fallback with reduced cluster-wide guarantees.
- MySQL slow: API latency rises; Hikari metrics and query plans guide mitigation.
- Broker down: outbox events remain pending.
- Bad deploy: rollback via previous image or Kubernetes rollout undo.

## Monitoring Notes
Track RED metrics, JVM, DB pool, Redis, cache hit/miss, and outbox age.

## Rollback Notes
Keep migrations backward-compatible and roll back image versions independently from destructive schema changes.

## Practical Example
Read post flow: `GET /posts/{id}` authenticates optional JWT, loads post from MySQL, increments Redis view count, returns DTO, and logs correlation ID.

## Interview Questions
- Where is the source of truth?
- What happens when Redis fails?
- How does the architecture support async notifications?

## Common Mistakes
- Claiming microservices when the project is a modular monolith.
- Ignoring operational dependencies.
- No explanation for consistency trade-offs.

## Self-Check
- [ ] I can draw the architecture in 2 minutes.
- [ ] I can trace login and post read flows.
- [ ] I can name top bottlenecks and mitigations.

