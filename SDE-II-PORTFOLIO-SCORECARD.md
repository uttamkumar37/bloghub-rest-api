# SDE-II Portfolio Scorecard

## Purpose
Score BlogHub as an SDE-II backend portfolio project and make missing work explicit.

## Practical Usage Steps
1. Score each category out of 10.
2. Attach evidence: code, tests, docs, metrics, screenshots, or run output.
3. Re-score after every hardening pass.

## Scorecard
| Category | Current score | Target | Current state | Missing work | Evidence required |
|---|---:|---:|---|---|---|
| Backend depth | 8 | 9 | Auth, posts, comments, likes, outbox, file upload boundary | Outbox publisher worker | Service code + tests |
| Security | 8 | 9 | Refresh rotation, blacklist, lockout, headers, CORS | More controller security tests | Auth test report |
| Database design | 8 | 9 | Flyway, indexes, soft delete, keyset path | Testcontainers MySQL query tests | Migration + EXPLAIN docs |
| API design | 8 | 9 | Versioning, validation, errors, idempotency guard | More OpenAPI examples | Swagger + tests |
| Testing | 6 | 9 | Existing unit/controller tests | Redis/Testcontainers/security coverage | JaCoCo + CI artifacts |
| Observability | 8 | 9 | Actuator, Prometheus, JSON logs, request ID | Grafana dashboard JSON | Metrics screenshot/export |
| Deployment | 8 | 9 | Docker Compose, health checks, K8s/Helm skeleton | Production secrets integration | Deploy docs + manifests |
| Scalability | 7 | 9 | Redis, keyset, indexes, outbox design | Load test evidence | k6/JMeter report |
| Maintainability | 8 | 9 | Layered services, docs, migrations | More package-level architecture tests | Code + docs |
| Interview explanation | 9 | 10 | Deep-dive docs and SDE-II explanation | Demo screenshots/live URL | README + demo |

## Final Score
- Before upgrade: 6.5/10 normal full-stack project.
- After this upgrade pass: 8.0/10 backend-focused SDE-II portfolio foundation.
- Target after remaining tests/load evidence: 9.0+/10.

## Implementation Notes
The strongest current evidence is backend architecture and production-readiness design. The biggest remaining gap is executable proof: integration tests, load tests, and dashboard artifacts.

## Failure Modes
- A project can look production-grade in docs but fail the bar without tests and metrics.
- Overclaiming designed-only features hurts interview credibility.

## Monitoring Notes
Use CI, JaCoCo, Prometheus, and load-test reports as recurring score inputs.

## Rollback Notes
If a feature is unstable, downgrade its score and mark it as planned rather than removing all related documentation.

## Practical Example
Security score improves only when refresh rotation has both implementation and a passing test, not just a design note.

## Interview Questions
- Which category is weakest and how would you improve it?
- What evidence proves your score?
- What trade-off did you make intentionally?

## Common Mistakes
- Scoring based on feature count instead of depth.
- No evidence column.
- Ignoring tests and observability.

## Self-Check
- [ ] Every score has evidence.
- [ ] Missing work is explicit.
- [ ] Final score is defensible in an interview.

