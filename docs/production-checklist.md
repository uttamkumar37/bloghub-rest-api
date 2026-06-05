# Production Checklist

## Purpose
Define the go/no-go checklist before presenting BlogHub as a production-grade SDE-II portfolio project.

## Practical Usage Steps
1. Complete each checklist item.
2. Link evidence in PRs or README.
3. Run load, security, and integration checks.
4. Update the portfolio scorecard.

## Implementation Notes
- This checklist covers backend depth, security, DB, Redis, observability, testing, and deployment.
- Missing items should become explicit TODOs rather than hidden claims.

## Checklist
- [ ] Flyway migrations run from empty database.
- [ ] JWT login, refresh, rotation, and logout tests pass.
- [ ] Redis blacklist/rate-limit behavior tested.
- [ ] Soft delete and keyset pagination tested.
- [ ] Actuator health/metrics/prometheus verified.
- [ ] Docker Compose starts all core services.
- [ ] CI uploads test and coverage reports.
- [ ] README has SDE-II interview explanation.

## Portfolio Metrics
| Metric | Target |
|---|---:|
| p95 read latency | < 300 ms |
| Throughput | 100 RPS local portfolio load |
| Error rate | < 1% 5xx |
| Cache hit ratio | > 70% hot reads |
| Startup time | < 20 seconds after dependencies healthy |
| Coverage | 75% overall, 85% service layer |

## Failure Modes
- Overclaiming features that are only documented.
- No evidence for performance or coverage numbers.
- Tests depend on local infrastructure unintentionally.

## Monitoring Notes
Use Prometheus/Grafana plus CI artifacts as evidence sources.

## Rollback Notes
If a production feature is unstable, keep the code isolated and document it as planned rather than calling it complete.

## Practical Example
Before an interview, run:
```bash
mvn clean test
docker compose config
```

## Interview Questions
- What evidence proves this project is production-ready?
- What would you improve next with one more month?
- Which risks remain?

## Common Mistakes
- Treating documentation as implementation.
- No measurable targets.
- Ignoring operational failure modes.

## Self-Check
- [ ] Every major claim has code, doc, test, or clear TODO evidence.
- [ ] Remaining risks are honest and prioritized.
- [ ] Portfolio metrics are measurable.

