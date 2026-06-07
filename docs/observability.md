# Observability

## Purpose
Define health checks, metrics, logs, traces-ready correlation IDs, dashboards, and alerting targets.

## Practical Usage Steps
1. Start the backend and visit `/api/v1/actuator/health`.
2. Scrape `/api/v1/actuator/prometheus` with Prometheus.
3. Use `X-Request-ID` to trace logs for a single request.
4. Track portfolio metrics after load tests.

## Implementation Notes
- Actuator exposes `health`, `info`, `metrics`, and `prometheus`.
- `CorrelationIdFilter` sets/propagates `X-Request-ID`.
- Production logs are JSON via `logback-spring.xml`.
- Micrometer common tags identify `application=bloghub-backend`.
- Cache service emits `bloghub.cache.requests` counters.

## Alerting Rules
| Signal | Target |
|---|---|
| p95 latency | < 300 ms for read APIs under portfolio load |
| Error rate | < 1% 5xx over 5 minutes |
| Cache hit ratio | > 70% on cacheable hot reads |
| DB pool pending | 0 sustained |
| Startup time | < 20 seconds locally after dependencies are healthy |

## Failure Modes
- Missing correlation IDs make incident debugging slow.
- Metrics endpoint exposed publicly leaks operational details.
- JSON logs without request IDs are hard to aggregate by incident.

## Monitoring Notes
- Dashboard panels: request rate, p95/p99 latency, 4xx/5xx, JVM heap/GC, Hikari pool, Redis latency, cache hit/miss.
- Add DB slow-query dashboard from MySQL exporter in a later iteration.

## Rollback Notes
If JSON logging breaks log ingestion, switch the active profile off `prod` or revert `logback-spring.xml` while retaining correlation IDs.

## Practical Example
```bash
curl -H 'X-Request-ID: demo-123' http://localhost:8080/api/v1/posts
```

## Interview Questions
- Which RED metrics do you expose for a REST API?
- How does a correlation ID help during incidents?
- What alerts would you create before production launch?

## Common Mistakes
- Exposing all actuator endpoints publicly.
- Alerting on CPU alone without user impact.
- Logging sensitive tokens or passwords.

## Self-Check
- [ ] Health, metrics, and Prometheus endpoints work.
- [ ] Logs contain correlation ID.
- [ ] Metrics include latency/error/cache signals.
- [ ] Alert targets are documented.
