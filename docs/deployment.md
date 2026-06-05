# Deployment

## Purpose
Document Docker Compose, Kubernetes, environment variables, secrets, rollback, and blue-green deployment strategy.

## Practical Usage Steps
1. Create `.env` from `.env.example`.
2. Start local dependencies with `docker compose up --build`.
3. Enable observability with `docker compose --profile observability up`.
4. For Kubernetes, apply manifests in `deploy/k8s`.
5. Use immutable image tags in production.

## Implementation Notes
- Backend Dockerfile is multi-stage and runs as non-root.
- Compose includes MySQL, Redis, MinIO, backend, frontend, Prometheus, and Grafana.
- Health checks are configured for MySQL, Redis, backend, and frontend.
- Flyway runs migrations on backend startup.

## Environment Variables
Key variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS`, `CORS_ALLOWED_ORIGIN_PATTERNS`, `APP_BOOTSTRAP_ADMIN_PASSWORD`.

## Failure Modes
- Missing `JWT_SECRET`: auth is unsafe or startup should be rejected in hardened production.
- DB migration failure: backend should not serve traffic.
- Redis unavailable: auth protections degrade to local fallback.

## Monitoring Notes
- Watch container restarts, health checks, migration logs, p95 latency, and error rate after deploy.

## Rollback Notes
- Docker Compose: pin previous image tag and run `docker compose up -d`.
- Kubernetes: `kubectl rollout undo deployment/bloghub-backend`.
- Blue-green: route traffic back to previous color if health or SLO checks fail.

## Practical Example
```bash
docker compose --profile observability up --build
```

## Interview Questions
- How do you roll back a bad migration?
- Why run containers as non-root?
- How would you implement blue-green deployment?

## Common Mistakes
- Using `latest` tags in production rollouts.
- Storing secrets in Compose files.
- No readiness check before sending traffic.

## Self-Check
- [ ] Secrets are environment-managed.
- [ ] Health checks exist.
- [ ] Rollback path is documented.
- [ ] Migrations are versioned.

