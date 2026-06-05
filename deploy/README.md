# Deploy

## Purpose
Collect Docker, Kubernetes, and Helm deployment assets for BlogHub.

## Practical Usage Steps
1. Use `docker-compose.yml` for local development and portfolio demos.
2. Use `deploy/docker/prometheus.yml` with the Compose observability profile.
3. Use `deploy/k8s` as a Kubernetes starter.
4. Use `deploy/helm` as the future chart boundary.

## Implementation Notes
- Secrets are represented with `secret.example.yaml`; real secrets should come from a secrets manager or sealed secrets.
- Backend health probes use `/api/v1/actuator/health`.
- Prometheus scrapes `/api/v1/actuator/prometheus`.
- Image tags should be immutable SHAs in real production.

## Failure Modes
- Bad migration prevents backend readiness.
- Redis outage degrades blacklist/rate-limit guarantees.
- Misconfigured CORS blocks frontend requests.
- `latest` image tag makes rollback ambiguous.

## Monitoring Notes
- Watch rollout status, pod restarts, readiness failures, p95 latency, 5xx, and DB pool saturation.

## Rollback Notes
```bash
kubectl rollout undo deployment/bloghub-backend
```
For Compose, pin the previous image digest and run `docker compose up -d`.

## Practical Example
```bash
docker compose --profile observability up --build
```

## Interview Questions
- How does readiness differ from liveness?
- How do you handle secrets in Kubernetes?
- How would you do blue-green deployment?

## Common Mistakes
- Committing real secrets.
- No resource requests/limits.
- No rollback command.

## Self-Check
- [ ] Health probes are configured.
- [ ] Secrets are externalized.
- [ ] Rollback path is documented.
- [ ] Observability config is included.

