# Schema Design

## Purpose
Explain BlogHub's relational model and the design choices behind users, roles, posts, comments, likes, refresh tokens, and outbox events.

## Practical Usage Steps
1. Read `V1__baseline_schema.sql`.
2. Compare each table to its JPA entity.
3. Verify constraints and indexes match product access patterns.
4. Use this file to explain the project in database-design interviews.

## Implementation Notes
- `users`, `posts`, and `comments` have audit and soft-delete columns.
- `post_likes` uses a composite primary key to prevent duplicate likes.
- `refresh_tokens` stores token hashes, not raw token values.
- `outbox_events` stores async work reliably before broker integration.

## Entity Relationships
```text
User 1..N Post
User 1..N Comment
Post 1..N Comment
User N..M Role
User N..M Post through post_likes
User 1..N RefreshToken
Aggregate 1..N OutboxEvent
```

## Failure Modes
- Missing unique constraints allow duplicate roles, likes, or usernames.
- Hard deletes make recovery and audit difficult.
- Over-normalizing hot feed counters can add avoidable joins.

## Monitoring Notes
- Track row growth for posts, comments, refresh tokens, and outbox events.
- Add cleanup jobs for expired refresh tokens and published outbox rows.

## Rollback Notes
Prefer additive schema changes. For soft delete, keep hard-delete rollback scripts separate and audited.

## Practical Example
Duplicate like prevention:
```sql
PRIMARY KEY (post_id, user_id)
```
The service can safely treat duplicate insert as "already liked" if idempotent like creation is added later.

## Interview Questions
- Why model likes as a join table?
- Why keep refresh tokens in SQL instead of Redis only?
- What would you denormalize first for a high-traffic feed?

## Common Mistakes
- Storing comma-separated tags or role names.
- Putting raw refresh tokens in the database.
- Forgetting cleanup strategy for token/outbox tables.

## Self-Check
- [ ] Every table has a clear owner and lifecycle.
- [ ] Constraints protect key business invariants.
- [ ] Indexes support real API queries.
- [ ] Sensitive tokens are hashed.

