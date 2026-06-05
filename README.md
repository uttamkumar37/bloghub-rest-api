# BlogHub REST API - Production-Grade SDE-II Backend Portfolio

A backend-focused portfolio project built with **Spring Boot 3.5**, **Java 17**, **MySQL**, **Redis**, **Flyway**, **Docker**, and **React 18**. The project demonstrates production-grade backend decisions across authentication, database design, caching, observability, async processing, deployment, and testing.

## Why This Project Matters

BlogHub is intentionally more than CRUD. It models the backend concerns an SDE-II engineer is expected to own: secure auth lifecycle, schema migrations, query performance, cache invalidation, incident debugging, operational metrics, release safety, and clear trade-off communication.

## Production-Grade Highlights

| Area | Implementation |
|---|---|
| Security | JWT access tokens, refresh token rotation, Redis blacklist, logout, rate-limited auth, strong passwords, account lockout, security headers, safe admin bootstrap |
| Database | Flyway migrations, soft delete, audit fields, composite indexes, keyset pagination, transaction boundaries |
| Redis | Token blacklist, rate limiting, idempotency, cache service, post view counters |
| Observability | Actuator health/metrics/prometheus, JSON logs, `X-Request-ID`, cache hit/miss metrics, runbook |
| Async | Outbox event table and event types for email verification, comments, and likes |
| Deployment | Docker Compose with MySQL/Redis/MinIO, Prometheus/Grafana profile, K8s manifests, Helm skeleton |
| API Maturity | Versioned base path, validation, standard errors, optional idempotency key, OpenAPI |
| Portfolio Evidence | Architecture docs, trade-offs, scorecard, production checklist, interview explanation |

## Architecture

```text
React/Nginx
   |
   v
Spring Boot API (/api/v1)
   |-- Spring Security + JWT + refresh tokens
   |-- Services: posts, comments, users, auth, files, outbox
   |-- Redis: blacklist, rate limits, idempotency, counters/cache
   |-- MySQL: users, roles, posts, comments, likes, refresh tokens, outbox
   |-- Actuator/Prometheus + JSON logs + X-Request-ID
   v
Docker Compose / Kubernetes-ready deployment assets
```

## Portfolio Metrics Targets

| Metric | Target |
|---|---:|
| p95 read latency | < 300 ms under portfolio load |
| Throughput | 100 RPS local portfolio load test |
| 5xx error rate | < 1% over 5 minutes |
| Cache hit ratio | > 70% for hot cacheable reads |
| Startup time | < 20 seconds after dependencies are healthy |
| Test coverage target | 75% overall, 85% service layer |

## Screenshots / Demo

- Live demo: placeholder
- Swagger screenshot: placeholder
- Grafana dashboard screenshot: placeholder
- Frontend screenshot: placeholder

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [Docker (recommended)](#docker-recommended)
- [Environment Variables](#environment-variables)
- [Running Tests](#running-tests)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements)

---

## Features

- **JWT Authentication** — stateless access tokens with refresh-token rotation
- **Logout / Token Revocation** — Redis-backed access-token blacklist
- **Auth Protection** — strong passwords, rate limiting, account lockout
- **Role-based Access Control** — `ROLE_USER` and `ROLE_ADMIN`
- **Posts** — full CRUD, category filtering, full-text search, pagination
- **Keyset Pagination** — cursor-style endpoint for scalable feed reads
- **Comments** — nested comment thread per post
- **Likes** — toggle like/unlike on posts
- **User Profiles** — view and update profile; admin can manage any user
- **File Upload Boundary** — post cover image upload with safe filenames and content-type checks
- **Flyway Migrations** — versioned schema with production validation
- **Redis** — blacklist, rate limiting, idempotency, counters, cache support
- **Outbox Events** — email verification, comment notification, post liked events
- **Observability** — Actuator, Prometheus, JSON logs, correlation IDs
- **Swagger / OpenAPI 3** — interactive docs at `/api/v1/swagger-ui.html`
- **Docker** — Compose spins up MySQL + Redis + MinIO + backend + frontend
- **CI/CD** — GitHub Actions pipeline: test → build → push GHCR images → deploy

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|-------------------------------------------------|
| Language    | Java 17, JavaScript (ES2022)                    |
| Framework   | Spring Boot 3.5.4, React 18                     |
| Security    | Spring Security 6, JWT (jjwt 0.12.3)           |
| Persistence | Spring Data JPA, Hibernate, MySQL 8             |
| Cache/Infra | Redis 7, Flyway, MinIO-compatible upload design |
| Observability | Actuator, Micrometer, Prometheus, Grafana docs |
| API Docs    | SpringDoc OpenAPI 2 (Swagger UI)                |
| Frontend    | Vite 5, Redux Toolkit 2, MUI v5, React Router 6 |
| HTTP Client | Axios 1.6                                       |
| Testing     | JUnit 5, Mockito, H2 (in-memory)                |
| Container   | Docker, Docker Compose, Nginx                   |
| CI/CD       | GitHub Actions, GHCR                            |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        Docker Compose                        │
│                                                              │
│  ┌─────────────────┐   /api/*   ┌──────────────────────┐    │
│  │  Nginx (port 80) │ ────────▶ │ Spring Boot (8080)   │    │
│  │  React SPA       │           │ REST API + JWT Auth   │    │
│  └─────────────────┘           │ Spring Data JPA        │    │
│                                 └──────────┬─────────────┘   │
│                                            │                  │
│                                 ┌──────────▼─────────────┐   │
│                                 │ MySQL 8 + Flyway        │   │
│                                 │ Redis + MinIO optional  │   │
│                                 └────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

## API Endpoints

All routes are prefixed with `/api/v1`.

### Auth

| Method | Path                   | Auth | Description          |
|--------|------------------------|------|----------------------|
| POST   | `/auth/register`       | —    | Register new user    |
| POST   | `/auth/login`          | —    | Login, returns access + refresh tokens |
| POST   | `/auth/refresh`        | —    | Rotate refresh token |
| POST   | `/auth/logout`         | Required | Blacklist access token and revoke refresh token |

### Posts

| Method | Path                          | Auth     | Description                       |
|--------|-------------------------------|----------|-----------------------------------|
| GET    | `/posts`                      | —        | List posts (paginated)            |
| GET    | `/posts/keyset`               | —        | List posts with keyset pagination |
| GET    | `/posts/{id}`                 | —        | Get post by ID                    |
| GET    | `/posts/search?query=`        | —        | Full-text search                  |
| GET    | `/posts/category/{category}`  | —        | Filter by category                |
| GET    | `/posts/user/{userId}`        | —        | Posts by specific user            |
| POST   | `/posts`                      | Required | Create post                       |
| PUT    | `/posts/{id}`                 | Required | Update post (author or admin)     |
| DELETE | `/posts/{id}`                 | Required | Delete post (author or admin)     |
| POST   | `/posts/{id}/like`            | Required | Toggle like                       |

### Comments

| Method | Path                                  | Auth     | Description                          |
|--------|---------------------------------------|----------|--------------------------------------|
| GET    | `/posts/{postId}/comments`            | —        | List comments for post               |
| POST   | `/posts/{postId}/comments`            | Required | Add comment                          |
| PUT    | `/posts/{postId}/comments/{id}`       | Required | Update comment (author or admin)     |
| DELETE | `/posts/{postId}/comments/{id}`       | Required | Delete comment (author or admin)     |

### Users

| Method | Path            | Auth     | Description                         |
|--------|-----------------|----------|-------------------------------------|
| GET    | `/users/me`     | Required | Get authenticated user profile      |
| GET    | `/users/{id}`   | —        | Get public user profile             |
| PUT    | `/users/{id}`   | Required | Update profile (owner only)         |
| DELETE | `/users/{id}`   | Required | Delete account (owner only)         |

### Files

| Method | Path                  | Auth     | Description              |
|--------|-----------------------|----------|--------------------------|
| POST   | `/files/post-cover`   | Required | Upload post cover image  |

Full interactive documentation is available at **`http://localhost:8080/api/v1/swagger-ui.html`** when the backend is running.

---

## Getting Started

### Prerequisites

- Git
- For local development: Java 17+, Maven 3.9+, Node 20+, MySQL 8
- For Docker: Docker Desktop 4.x+ with Compose v2

### Local Development

#### 1. Clone the repository

```bash
git clone https://github.com/your-username/bloghub-rest-api.git
cd bloghub-rest-api
```

#### 2. Set up MySQL

```sql
CREATE DATABASE bloghub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bloghub_user'@'localhost' IDENTIFIED BY 'bloghub_pass';
GRANT ALL PRIVILEGES ON bloghub_db.* TO 'bloghub_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. Start the backend

```bash
cd backend
export DB_URL="jdbc:mysql://localhost:3306/bloghub_db?useSSL=false&serverTimezone=UTC"
export DB_USERNAME=bloghub_user
export DB_PASSWORD=bloghub_pass
export JWT_SECRET=change_me_use_a_long_random_secret_at_least_64_chars
export JWT_EXPIRATION_MS=900000
./mvnw spring-boot:run
```

The API starts on **`http://localhost:8080`**.

> Admin bootstrap is disabled by default. To create a local admin explicitly, set `APP_BOOTSTRAP_ADMIN_ENABLED=true` and provide `APP_BOOTSTRAP_ADMIN_PASSWORD` through environment variables.

#### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app is available at **`http://localhost:5173`** and proxies `/api` calls to the backend automatically.

---

### Docker (recommended)

#### 1. Create your `.env` file

```bash
cp .env.example .env
# Edit .env and set a strong JWT_SECRET
```

#### 2. Start all services

```bash
docker compose up --build
```

| Service  | URL                                              |
|----------|--------------------------------------------------|
| Frontend | http://localhost                                 |
| Backend  | http://localhost:8080/api/v1                     |
| Swagger  | http://localhost:8080/api/v1/swagger-ui.html     |
| MySQL    | localhost:3306 (bloghub_db)                      |
| Redis    | localhost:6379                                   |
| MinIO    | http://localhost:9001                            |
| Prometheus | http://localhost:9090 with `--profile observability` |
| Grafana  | http://localhost:3001 with `--profile observability` |

#### 3. Stop services

```bash
docker compose down          # keep data volume
docker compose down -v       # also remove MySQL data
```

---

## Environment Variables

Copy `.env.example` to `.env` and adjust the values.

| Variable             | Required | Default        | Description                           |
|----------------------|----------|----------------|---------------------------------------|
| `MYSQL_ROOT_PASSWORD`| Yes      | rootpassword   | MySQL root password                   |
| `MYSQL_DATABASE`     | Yes      | bloghub_db     | Database name                         |
| `MYSQL_USER`         | Yes      | bloghub_user   | Application DB user                   |
| `MYSQL_PASSWORD`     | Yes      | bloghub_pass   | Application DB password               |
| `JWT_SECRET`         | **Yes**  | *(none)*       | ≥64-char random/base64 string for HMAC signing |
| `JWT_EXPIRATION_MS`  | No       | 900000         | Access token lifetime in milliseconds |
| `JWT_REFRESH_EXPIRATION_MS` | No | 604800000 | Refresh token lifetime in milliseconds |
| `REDIS_HOST`         | Yes      | redis          | Redis host for Docker |
| `APP_BOOTSTRAP_ADMIN_ENABLED` | No | false | Explicit admin bootstrap switch |
| `APP_BOOTSTRAP_ADMIN_PASSWORD` | If bootstrap enabled | *(none)* | Strong admin bootstrap password |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | No | localhost origins | Comma-separated allowed origins |

Generate a secure JWT secret:

```bash
openssl rand -base64 64
```

---

## Running Tests

```bash
cd backend
./mvnw clean verify          # compile + unit tests + JaCoCo coverage
```

Coverage report is generated at `backend/target/site/jacoco/index.html`.

The test suite uses an **H2 in-memory database** (`@ActiveProfiles("test")`); no external services are required.

Production hardening target: add Testcontainers MySQL and Redis integration suites, then enforce 75% overall and 85% service-layer coverage in CI.

---

## Production Documentation

| Topic | Document |
|---|---|
| Architecture | [docs/architecture-deep-dive.md](docs/architecture-deep-dive.md) |
| System design | [docs/system-design.md](docs/system-design.md) |
| Security | [docs/security.md](docs/security.md) |
| Auth flow | [docs/auth-flow.md](docs/auth-flow.md) |
| Database performance | [docs/database-performance.md](docs/database-performance.md) |
| Redis caching | [docs/redis-caching.md](docs/redis-caching.md) |
| Observability | [docs/observability.md](docs/observability.md) |
| Async events | [docs/async-events.md](docs/async-events.md) |
| Outbox | [docs/outbox-pattern.md](docs/outbox-pattern.md) |
| File upload | [docs/file-upload.md](docs/file-upload.md) |
| API design | [docs/api-design.md](docs/api-design.md) |
| Testing | [docs/testing-strategy.md](docs/testing-strategy.md) |
| Deployment | [docs/deployment.md](docs/deployment.md) |
| Runbook | [docs/runbook.md](docs/runbook.md) |
| Trade-offs | [docs/tradeoffs.md](docs/tradeoffs.md) |
| Interview explanation | [docs/interview-explanation.md](docs/interview-explanation.md) |
| Portfolio score | [SDE-II-PORTFOLIO-SCORECARD.md](SDE-II-PORTFOLIO-SCORECARD.md) |

---

## Deployment

### Docker on a Linux VM (EC2 / DigitalOcean / Hetzner)

```bash
# On the server
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker

mkdir /opt/bloghub && cd /opt/bloghub

# Copy docker-compose.yml and your .env file, then:
docker compose up -d
```

### Render (free tier)

1. Create a **MySQL** database add-on and copy the connection string.
2. Create a **Web Service** pointing to `./backend`, set `DOCKER_BUILDPACK=true`.
3. Add all environment variables in the Render dashboard.
4. For the frontend create a separate **Static Site** service (build command: `npm run build`, publish directory: `dist`).

### Railway

```bash
railway login
railway init
railway add --service mysql
railway up
```

Set env vars via the Railway dashboard.

---

## Project Structure

```
bloghub-rest-api/
├── backend/                        # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bloghub/api/
│   │   │   │   ├── config/         # Security, OpenAPI, DataInitializer
│   │   │   │   ├── controller/     # REST controllers
│   │   │   │   ├── dto/            # Request/response DTOs
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── exception/      # Custom exceptions + global handler
│   │   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   │   ├── security/       # JWT filter, entry point, UserDetails
│   │   │   │   └── service/        # Business logic
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-prod.properties
│   │   └── test/                   # JUnit 5 + Mockito test suites
│   └── Dockerfile
├── frontend/                       # React 18 SPA
│   ├── src/
│   │   ├── components/             # Navbar, Footer, PostCard, CommentSection
│   │   ├── pages/                  # Login, Register, Dashboard, Post*, Profile
│   │   ├── redux/                  # store, authSlice, postSlice
│   │   └── services/               # apiClient, authService, postService, commentService
│   ├── nginx.conf
│   └── Dockerfile
├── docs/
│   └── schema.sql                  # Full MySQL schema with indexes + seed data
├── .github/
│   └── workflows/
│       └── ci-cd.yml               # GitHub Actions pipeline
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## How I Would Explain This Project in an SDE-II Interview

**Problem statement:** BlogHub is a blog platform that supports authenticated authors, public readers, posts, comments, likes, search, and profile management.

**Architecture:** It is a modular Spring Boot backend with MySQL as source of truth, Redis for ephemeral distributed state, Flyway for schema versioning, and Docker/Kubernetes deployment assets.

**Key backend decisions:** I added refresh token rotation, Redis blacklist, auth rate limiting, soft delete, keyset pagination, outbox events, structured logs, Prometheus metrics, and safe admin bootstrap.

**Scaling strategy:** Reads scale through indexes, keyset pagination, Redis caching/counters, and horizontal backend replicas. Async notifications move through an outbox boundary.

**Failure handling:** Redis fallback is documented, DB pool/query latency is observable, outbox protects against broker outages, and rollback paths are defined for Compose and Kubernetes.

**Security:** Short-lived access tokens, opaque refresh tokens, token revocation, strong passwords, lockout, CORS, and security headers address common portfolio gaps.

**Observability:** Actuator, Prometheus, JSON logs, request IDs, runbooks, and metrics targets make incidents explainable.

**Trade-offs:** I kept it as a modular monolith because the goal is backend depth without unnecessary microservice overhead. The next scale trigger would split notification/search workloads.

**What I would improve next:** add Testcontainers coverage, implement the outbox publisher worker, add load-test evidence, wire S3/MinIO production storage, and export a Grafana dashboard.

---

## Future Roadmap

- [ ] Full email verification delivery worker (Spring Mail or external provider)
- [ ] Tag system for posts (many-to-many)
- [ ] Durable post view-count reconciliation job
- [ ] WebSocket or async notifications for new comments
- [ ] Outbox publisher worker with RabbitMQ/Kafka
- [ ] Testcontainers MySQL and Redis integration coverage
- [ ] k6/JMeter load-test report and Grafana dashboard JSON
- [ ] S3-backed file storage implementation
- [ ] React Native mobile client
