# Runbook

## Purpose
Provide production operating steps for common BlogHub incidents.

## Practical Usage Steps
1. Identify user impact and affected endpoint.
2. Check health, logs, metrics, and recent deploys.
3. Mitigate with rollback, scale-out, or feature disablement.
4. Capture evidence before restart.
5. Open a postmortem for P1/P0 incidents.

## Implementation Notes
- Use `/actuator/health` for dependency status.
- Use Prometheus for p95, error rate, JVM, Hikari, Redis, and cache metrics.
- Use `X-Request-ID` in logs for single-request tracing.

## Incident Checks
```bash
curl -s http://localhost:8080/api/v1/actuator/health
docker logs bloghub-backend --since 30m
docker stats
docker compose ps
```

Database:
```sql
SHOW PROCESSLIST;
EXPLAIN SELECT * FROM posts ORDER BY created_at DESC LIMIT 10;
```

## Failure Modes
- Login failures: check Redis/rate limit, DB, account lockout, JWT secret.
- Slow feed: check DB plan, keyset pagination, Hikari pending, cache errors.
- Upload failures: check file size/type, disk volume, upload directory permissions.

## Monitoring Notes
- Page on sustained 5xx rate, health down, DB pool timeout, Redis unavailable, or p95 breach.
- Ticket non-page issues for high 4xx, low cache hit ratio, or slow startup.

## Rollback Notes
Use immutable Docker image tags. For Compose, pull the previous image digest and restart. For Kubernetes, use `kubectl rollout undo`.

## Practical Example
Slow API triage:
1. Check `/actuator/metrics/http.server.requests`.
2. Filter logs by `X-Request-ID`.
3. Run `EXPLAIN` for the query behind the route.
4. Roll back if a recent deploy changed query behavior.

## Interview Questions
- What do you check first during a latency incident?
- How do you decide rollback vs hotfix?
- What evidence do you capture before restarting?

## Common Mistakes
- Restarting before collecting evidence.
- Debugging DB without checking pool saturation.
- Closing incidents without prevention actions.

## Self-Check
- [ ] I can find health, logs, and metrics quickly.
- [ ] I can name rollback command per deployment target.
- [ ] I can separate mitigation from root-cause fix.

