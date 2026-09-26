# Repository Copilot Instructions

## Repository Overview

**bloghub-rest-api** (BlogHub) is a backend-focused portfolio project: a production-style blogging REST API with a React frontend. It exists to demonstrate SDE-II-level backend decisions: auth lifecycle, schema migrations, keyset pagination, Redis usage, an outbox pattern, observability and deployment assets. It is a portfolio/learning project, so honesty about what is and is not measured matters (see the AI rules below).

## Technology Stack

- Backend: Java 17, Spring Boot 3.5.4, Maven (`backend/pom.xml`), Spring Web, Spring Data JPA/Hibernate, Spring Security 6 with JWT (jjwt 0.12.3), Spring Cache, Bean Validation, Lombok, MapStruct (version pinned in the pom), springdoc-openapi 2.8.6, Actuator/Micrometer/Prometheus, logstash-logback-encoder (JSON logs)
- Database: MySQL 8; Flyway migrations (`V1__baseline_schema.sql`)
- Cache/infra: Redis 7; MinIO (local S3-compatible upload storage)
- Frontend: React 18 (JavaScript/JSX), Vite 5, Redux Toolkit, MUI v5, React Router 6, Axios, react-hook-form, ESLint
- Testing: JUnit 5, Mockito, H2 (test profile)
- Containers/CI: Docker, Docker Compose, Nginx; GitHub Actions (`ci-cd.yml`) with GHCR images; Kubernetes manifests and a Helm skeleton under `deploy/`

## Repository Structure

```
backend/src/main/java/com/bloghub/api/
  controller/  Auth, Comment, FileUpload, Post, User controllers
  service/     AuthService, PostService, CommentService, RefreshTokenService, TokenBlacklistService,
               RateLimiterService, IdempotencyService, CacheService, FileStorageService; impl/ and outbox/
  repository/  Spring Data JPA repositories (incl. OutboxEventRepository)
  entity/      BaseEntity, User, Role, Post, Comment, RefreshToken, OutboxEvent
  dto/         request/response DTOs, ApiResponse, PagedResponse, KeysetPageResponse
  security/    JwtTokenProvider, JwtAuthenticationFilter, entry point, CustomUserDetailsService
  config/      SecurityConfig, RedisConfig, WebConfig, OpenApiConfig, ObservabilityConfig, DataInitializer
  filter/      CorrelationIdFilter (X-Request-ID)
  exception/   GlobalExceptionHandler, BlogApiException, ResourceNotFoundException
  event/       BlogEvent types for the outbox
backend/src/main/resources/  application.properties, application-prod.properties, logback-spring.xml, db/migration/
backend/src/test/java/       controller, security and service tests
frontend/src/                pages/, components/, redux/ (slices, store), services/ (Axios API clients)
deploy/                      docker/ (prometheus.yml), k8s/, helm/, README.md
docs/                        api-design, auth-flow, security, schema-design, redis-caching, outbox-pattern, observability, runbook, tradeoffs, etc.
docker-compose.yml, .env.example
```

## Architecture

Layered monolith: React/Nginx -> Spring Boot API (context path `/api/v1`) -> MySQL + Redis. Cross-cutting: JWT auth with refresh-token rotation, Redis-backed access-token blacklist, rate limiting and idempotency, an outbox table for async events (email verification, new comment, post liked), correlation IDs and Prometheus metrics. Design rationale is documented in `docs/`; read the relevant doc before changing that area.

## Development Commands

```bash
# Backend (from backend/)
mvn clean verify -B -Dspring.profiles.active=test   # what CI runs
mvn spring-boot:run

# Frontend (from frontend/)
npm ci && npm run dev
npm run lint
npm run build

# Full stack (MySQL, Redis, MinIO, backend, frontend)
docker compose up --build
docker compose config            # validated in CI
```

Swagger UI: `/api/v1/swagger-ui.html`. Copy `.env.example` to `.env` for local overrides.

## Coding Guidelines

- Constructor injection (Lombok `@RequiredArgsConstructor` style where the code already uses it); keep controllers thin and put logic in services (`service/impl` for implementations).
- Never expose entities; map to DTOs. Reuse `ApiResponse`, `PagedResponse` and `KeysetPageResponse` for API shapes.
- Errors go through `GlobalExceptionHandler` with `BlogApiException`/`ResourceNotFoundException`; do not return ad-hoc error bodies.
- Keep transaction boundaries on the service layer; be deliberate with `@Transactional` and read-only queries.
- Prefer keyset pagination for feeds; keep composite indexes in step with query changes.
- Soft delete and audit fields come from `BaseEntity`; follow that pattern for new entities.
- Frontend: functional components with hooks; Redux Toolkit slices in `redux/slices`; server calls only through `services/`; keep MUI as the UI library. The frontend is plain JavaScript (JSX), not TypeScript.

## Testing

- Backend tests under `backend/src/test/java/com/bloghub/api/` (controller, security, service; JUnit 5 + Mockito + H2). Existing coverage is limited (about 8 test classes); add tests for every behavior change, especially auth, rate limiting, blacklist and outbox logic.
- The README states targets (75% overall, 85% service layer). These are targets, not measured results.
- Frontend has no test runner configured; `npm run lint` and `npm run build` are the checks.

## API / Data Rules

- All endpoints are under `/api/v1`, versioned via the context path. Use standard validation annotations on request DTOs.
- Schema changes are new Flyway migrations in `db/migration`; never edit `V1__baseline_schema.sql`. Hibernate must not be relied on to change production schema.
- Optional `Idempotency-Key` handling exists via `IdempotencyService`; keep write endpoints that support it consistent.
- Outbox events are written in the same transaction as the state change they describe.

## Security

- JWT access tokens (`app.jwt.*`) plus refresh-token rotation; logout blacklists tokens in Redis; auth endpoints are rate limited; strong-password rules and account lockout; security headers; roles `ROLE_USER` and `ROLE_ADMIN`.
- Secrets come from environment variables (`JWT_SECRET`, `MYSQL_*`, `REDIS_PASSWORD`, `APP_BOOTSTRAP_ADMIN_*`, `MINIO_*`, `GRAFANA_*`; see `.env.example`). The fallback JWT secret and admin defaults in `application.properties` and compose are development-only; never reuse them for deployment and never commit real values. Bootstrap admin is disabled by default; keep it that way.
- Upload handling uses safe filenames and content-type checks; preserve those checks.
- CORS origins come from `CORS_ALLOWED_ORIGIN_PATTERNS`.

## Infrastructure / Deployment

Docker Compose (MySQL, Redis, MinIO, backend, frontend, optional Prometheus/Grafana profile), Kubernetes manifests in `deploy/k8s` (with `secret.example.yaml` only), Helm skeleton in `deploy/helm`. GitHub Actions `ci-cd.yml`: backend Maven verify, Docker/Compose validation, frontend lint + build, GHCR image publishing. See `deploy/README.md` and `docs/deployment.md`.

## Change Guidelines

1. Understand the existing implementation first.
2. Make the smallest coherent change.
3. Preserve current architecture unless there is a strong reason to change it.
4. Do not introduce a new library when the existing stack already solves the requirement.
5. Update tests for behavior changes.
6. Run relevant tests/build before considering the change complete.
7. Do not leave commented-out code.
8. Do not leave TODO placeholders unless explicitly requested.
9. Do not fabricate implementation status.
10. Do not claim something was tested unless it was actually executed.

## Code Quality Rules

- Prefer readable code over clever code; avoid unnecessary duplication and abstraction.
- Follow existing naming conventions and package layout (`com.bloghub.api.*`).
- Keep methods and components focused; handle edge cases (expired/revoked tokens, rate-limit windows, cache misses, Redis unavailability).
- Preserve backward compatibility of `/api/v1` responses.
- Avoid unrelated refactoring during focused changes.

## Git Commit Rules

- Never add a `Co-Authored-By` trailer unless I explicitly request it.
- Never add Claude, Anthropic, GitHub Copilot, OpenAI, ChatGPT, Codex, Cursor, or any AI tool as an author or co-author.
- Use only the configured Git `user.name` and `user.email`.
- Do not mention AI assistance in commit messages.
- Keep commit messages concise and professional.
- Do not commit automatically unless I explicitly ask.
- Do not push automatically unless I explicitly ask.
- Never force-push unless I explicitly request it.
- Never rewrite Git history unless I explicitly request it.

## Git Commit Attribution Rules

- Never add a `Co-Authored-By` trailer for an AI system.
- Never add Claude, Anthropic, GitHub Copilot, OpenAI, ChatGPT, Codex, Cursor, Gemini, Devin, or any AI tool as an author or co-author.
- Never change Git author/committer identity to an AI account.
- Use only the configured human Git `user.name` and `user.email`.
- Do not add “Generated by AI”, “Created with AI”, or similar attribution to commit messages.
- Keep commit messages focused on the technical change.
- Do not commit automatically unless explicitly requested.
- Do not push automatically unless explicitly requested.
- Never rewrite Git history unless explicitly requested.

## AI Assistant Working Rules

When working in this repository:

- Inspect existing code before proposing architecture changes.
- Do not assume a feature exists without verifying it.
- Do not create fake implementations to make UI or tests appear complete.
- Do not generate random metrics, scores, or placeholder business data unless explicitly requested as test/demo data.
- Clearly separate verified behavior from assumptions.
- Prefer completing working vertical slices over creating many unfinished placeholders.
- Preserve repository conventions.
- Avoid massive rewrites unless explicitly requested.
- When fixing a bug, identify the underlying cause where practical.
- When adding functionality, consider error handling and tests.
- Never expose secrets, API keys, tokens, or credentials.
- Never hardcode secrets.

## Repository-Specific Rules

- Keep docs in `docs/`, `SDE-II-PORTFOLIO-SCORECARD.md` and the README truthful. Performance numbers, coverage and latency figures in them are targets unless a real measurement backs them; never invent benchmark results, screenshots or a live demo URL (the README currently has explicit placeholders for those).
- When changing auth, caching, outbox or observability behavior, update the matching doc in `docs/` (for example `auth-flow.md`, `redis-caching.md`, `outbox-pattern.md`, `runbook.md`).
- Do not describe features (email delivery, event consumers) as working unless code exists; the outbox currently records events, and the docs describe async processing as a design.
- Keep the security posture at least as strict as today; never loosen rate limits, lockout, blacklist checks or CORS to make something easier to test.
