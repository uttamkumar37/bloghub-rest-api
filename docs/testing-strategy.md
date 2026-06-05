# Testing Strategy

## Purpose
Define the portfolio-level testing strategy for unit, controller, integration, security, repository, Redis, and CI coverage.

## Practical Usage Steps
1. Run `mvn clean test` locally.
2. Review JaCoCo at `backend/target/site/jacoco/index.html`.
3. Add tests for every new production feature.
4. Use Testcontainers for MySQL/Redis integration tests in the next hardening pass.

## Implementation Notes
- Existing tests cover auth service, posts service, post controller, JWT provider, and application startup.
- New auth tests should cover refresh rotation, logout blacklist, expired token, rate limit, unauthorized, and role-based access.
- Testcontainers dependencies are present for MySQL expansion.
- Target coverage: 75% overall and 85% service layer.

## Test Matrix
| Area | Test type |
|---|---|
| Auth | Unit + controller + security |
| Refresh token | Service integration |
| Redis blacklist/rate limit | Integration with Redis container |
| Repository/query | DataJpaTest/Testcontainers MySQL |
| File upload | Controller + service unit |
| Outbox | Service/repository test |

## Failure Modes
- H2 passing while MySQL fails due dialect differences.
- Security tests bypass filters accidentally.
- Tests mock away transaction behavior.

## Monitoring Notes
- CI should upload Surefire and JaCoCo artifacts.
- Track flaky tests separately from deterministic failures.

## Rollback Notes
Do not lower coverage targets to pass CI. Mark known gaps and add incremental tests.

## Practical Example
```bash
cd backend
mvn clean verify -Dspring.profiles.active=test
```

## Interview Questions
- What belongs in unit vs integration tests?
- Why use Testcontainers instead of only H2?
- Which security paths must never be untested?

## Common Mistakes
- Only testing happy paths.
- Using H2 for MySQL-specific migrations forever.
- Not testing unauthorized and forbidden cases.

## Self-Check
- [ ] Critical auth/security paths have tests.
- [ ] Pagination and soft delete have tests.
- [ ] CI uploads reports.
- [ ] Coverage gaps are explicit.

