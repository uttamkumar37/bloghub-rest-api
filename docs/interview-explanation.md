# Interview Explanation

## Purpose
Provide a polished SDE-II explanation of BlogHub for recruiter screens, hiring-manager rounds, and backend deep dives.

## Practical Usage Steps
1. Practice the 2-minute version.
2. Practice the 10-minute architecture version.
3. Prepare evidence: code paths, tests, docs, metrics, and TODOs.

## 2-Minute Explanation
BlogHub is a Spring Boot 3 backend for a blog platform with JWT auth, posts, comments, likes, profiles, search, and pagination. I upgraded it toward production readiness by adding refresh-token rotation, Redis-backed blacklist and rate limiting, Flyway migrations, soft delete, keyset pagination, outbox events, file upload boundaries, Actuator/Prometheus metrics, JSON logs, request correlation IDs, Docker Compose infrastructure, and production runbooks.

## Key Backend Decisions
- JWT access tokens are short-lived; refresh tokens are opaque and rotated.
- MySQL is the source of truth; Redis is used for fast ephemeral state.
- Flyway replaces ad hoc schema updates.
- Outbox gives reliable async event handoff.
- Keyset pagination avoids deep offset scans.

## Scaling Strategy
- Cache hot reads and counters in Redis.
- Index feed/search/category access paths.
- Add async workers for notifications.
- Use horizontal backend replicas with shared Redis/MySQL.

## Failure Handling
- Redis fallback keeps one-instance development working but is documented as a reduced guarantee.
- Outbox absorbs broker failures.
- Health checks, metrics, and request IDs reduce incident time.

## Security
Refresh rotation, token blacklist, strong passwords, lockout, CORS config, security headers, and disabled admin bootstrap reduce common portfolio-project risks.

## Observability
Actuator exposes health, metrics, and Prometheus. Logs include correlation ID. Portfolio SLOs are p95 latency, throughput, error rate, cache ratio, startup time, and coverage.

## Trade-offs
This is intentionally a modular monolith, not microservices. The design focuses on clean boundaries and production primitives without unnecessary distributed complexity.

## What I Would Improve Next
Add Testcontainers MySQL/Redis coverage, implement the outbox publisher worker, add S3/MinIO production storage implementation, run load tests, and add a Grafana dashboard JSON.

## Implementation Notes
Use this explanation with references to `docs/security.md`, `docs/database-performance.md`, `docs/observability.md`, and `SDE-II-PORTFOLIO-SCORECARD.md`.

## Failure Modes
Be honest about implemented vs designed pieces: outbox records are implemented; broker publishing is documented as next work.

## Monitoring Notes
Mention p95, 5xx, cache hit ratio, DB pool saturation, Redis latency, and outbox age.

## Rollback Notes
Explain image rollback, migration safety, and feature-flag/disable paths for cache and async publishers.

## Practical Example
"If a post feed becomes slow, I check p95 by route, DB pool pending, query plan, and cache hit ratio. Then I would move high-volume feeds to keyset pagination plus cache and verify with load tests."

## Interview Questions
- What was the hardest backend decision?
- How do you know the project is production-ready?
- What remains before real production?

## Common Mistakes
- Overstating unfinished work.
- Describing frontend details instead of backend decisions.
- No metrics or evidence.

## Self-Check
- [ ] I can explain the project in 2 minutes.
- [ ] I can go deep on auth, DB, cache, and observability.
- [ ] I can name unfinished work honestly.

