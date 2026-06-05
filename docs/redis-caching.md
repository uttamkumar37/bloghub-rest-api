# Redis Caching

## Purpose
Define Redis usage for blacklist, rate limiting, popular post cache, view counts, and optional search cache.

## Practical Usage Steps
1. Start Redis with `docker compose up redis`.
2. Configure `REDIS_HOST` and `REDIS_PORT`.
3. Use namespaced keys for every feature.
4. Apply TTLs and invalidation rules per data type.
5. Monitor hit/miss/error metrics.

## Implementation Notes
- Blacklist keys: `bloghub:auth:blacklist:<tokenHash>`.
- Rate-limit keys: `bloghub:ratelimit:<scope>`.
- View-count keys: `bloghub:post:view-count:<postId>`.
- Idempotency keys: `bloghub:idempotency:<operation>:<user>:<key>`.
- Cache wrappers catch Redis failures and degrade gracefully.

## TTL Strategy
| Use case | TTL | Reason |
|---|---:|---|
| Access-token blacklist | Remaining token lifetime | Avoid unbounded key growth |
| Login/register rate limit | 60 seconds | Limits bursts without blocking normal use |
| Popular posts | 5 minutes | Fast feed reads with acceptable staleness |
| Search results | 2 minutes | Avoid stale search for long periods |
| View counts | 6 hours | Batch/reconcile periodically |

## Invalidation Strategy
- Evict post list/search caches on post create, update, delete, or category change.
- Keep blacklist keys until token expiry.
- Reconcile view count cache to DB if a durable view-count column is added.

## Failure Modes
- Redis down: app continues with in-memory fallback for blacklist/rate/idempotency in one instance.
- Cache stampede: protect hot keys with TTL jitter or request coalescing when traffic grows.
- Stale reads: keep TTL low and invalidate on writes.

## Monitoring Notes
- `bloghub.cache.requests{result="hit|miss|error"}`.
- Redis command latency, memory usage, evictions, connected clients.
- Auth 429 rate and blacklist lookup errors.

## Rollback Notes
Disable cache usage per code path, keep Redis for blacklist/rate limiting, or scale Redis separately. Do not silently remove blacklist behavior in production.

## Practical Example
```text
bloghub:post:view-count:42 -> 187
TTL: 7 days from last increment
```

## Interview Questions
- What happens if Redis is unavailable?
- How do you choose TTLs?
- How would you prevent cache stampede for a popular post?

## Common Mistakes
- Caching user-specific responses under global keys.
- Forgetting invalidation after writes.
- Treating Redis as the source of truth for critical data without persistence strategy.

## Self-Check
- [ ] Every key has namespace and TTL.
- [ ] Every cache has invalidation or bounded staleness.
- [ ] Metrics distinguish hit, miss, and error.
- [ ] Redis fallback behavior is documented.

