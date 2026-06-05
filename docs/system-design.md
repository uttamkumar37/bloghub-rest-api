# System Design

## Purpose
Frame BlogHub as a system-design discussion for an SDE-II backend interview.

## Practical Usage Steps
1. Start with requirements and scale assumptions.
2. Define APIs, data model, and core flows.
3. Discuss scaling reads, writes, auth, cache, and async events.
4. Close with trade-offs and future improvements.

## Implementation Notes
- Core APIs: auth, posts, comments, likes, users, files.
- Core data: users, roles, posts, comments, likes, refresh tokens, outbox.
- Read scale: Redis cache, keyset pagination, DB indexes.
- Write reliability: transactions, soft delete, outbox.
- Security: JWT, refresh rotation, blacklist, CORS, headers.

## Scale Assumptions
- 100 RPS local target for portfolio load test.
- Read-heavy traffic: 80% reads, 20% writes.
- Hot posts benefit from cache and view counters.

## Failure Modes
- Cache stampede on popular posts.
- DB pool exhaustion during slow queries.
- Refresh token replay.
- Notification broker outage.

## Monitoring Notes
SLO: p95 read latency < 300 ms, 5xx < 1%, cache hit ratio > 70% for hot reads.

## Rollback Notes
Feature-flag cache-backed features and async publishers. Keep API contracts stable under `/api/v1`.

## Practical Example
For feed pagination, prefer:
```http
GET /api/v1/posts/keyset?cursor=9000&size=10
```
over deep offset scans.

## Interview Questions
- How would you scale the feed to millions of posts?
- How do you keep likes idempotent?
- What consistency do view counts need?

## Common Mistakes
- Starting with technology before requirements.
- No capacity estimate.
- No plan for cache invalidation.

## Self-Check
- [ ] Requirements, APIs, data, scale, and bottlenecks are covered.
- [ ] Each production feature maps to a real failure mode.
- [ ] Future roadmap is honest and prioritized.

