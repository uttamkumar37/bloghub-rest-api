# Database Performance

## Purpose
Document schema versioning, index strategy, query performance, pagination, N+1 prevention, soft delete, and transaction boundaries.

## Practical Usage Steps
1. Review Flyway migrations in `backend/src/main/resources/db/migration`.
2. Run `EXPLAIN` before adding or changing high-volume queries.
3. Prefer keyset pagination for feed-style scrolling.
4. Keep transactions short and avoid remote calls inside transactions.
5. Add tests for pagination, search, soft delete, and rollback behavior.

## Implementation Notes
- Flyway owns schema creation; Hibernate validates in non-test profiles.
- Posts use indexes on author, created time, category/created time, and deleted/created/id.
- Soft delete is implemented with Hibernate `@SQLDelete` and `@SQLRestriction`.
- Keyset endpoint: `GET /posts/keyset?cursor=<lastSeenId>&size=10`.
- `spring.jpa.open-in-view=false` prevents accidental lazy loading during serialization.

## Query Plan Example
```sql
EXPLAIN
SELECT *
FROM posts
WHERE deleted = false AND category = 'java'
ORDER BY created_at DESC
LIMIT 10;
```

Expected index: `idx_posts_category_created` or a future `(deleted, category, created_at, id)` composite index if category feed becomes hot.

## Failure Modes
- Offset pagination gets slower as offset grows.
- Missing index causes table scan and DB CPU spike.
- N+1 queries multiply latency under normal traffic.
- Soft-deleted rows leak if native queries skip the predicate.

## Monitoring Notes
- DB CPU, slow queries, lock waits, connection pool active/pending/timeout.
- API p95/p99 by route.
- Query count per request in integration tests.

## Rollback Notes
Use backward-compatible migrations: add nullable columns first, backfill, then enforce constraints. Avoid destructive column drops without rollback scripts.

## Practical Example
Offset problem:
```sql
SELECT * FROM posts ORDER BY created_at DESC LIMIT 10 OFFSET 50000;
```
Keyset alternative:
```sql
SELECT * FROM posts WHERE id < 900000 ORDER BY id DESC LIMIT 10;
```

## Interview Questions
- Why does offset pagination degrade?
- How do you find and fix N+1 queries in JPA?
- What indexes support a category feed?

## Common Mistakes
- Adding indexes without matching query predicates and ordering.
- Leaving `ddl-auto=update` in production.
- Ignoring transaction boundaries in service methods.

## Self-Check
- [ ] Every hot query has an index rationale.
- [ ] New schema changes are Flyway migrations.
- [ ] Large lists use keyset pagination where possible.
- [ ] Soft-delete behavior has tests.

