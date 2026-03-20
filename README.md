# BlogHub REST API

A production-ready full-stack blog application built with **Spring Boot 3** and **React 18**.

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

- **JWT Authentication** — stateless, HS512-signed tokens
- **Role-based Access Control** — `ROLE_USER` and `ROLE_ADMIN`
- **Posts** — full CRUD, category filtering, full-text search, pagination
- **Comments** — nested comment thread per post
- **Likes** — toggle like/unlike on posts
- **User Profiles** — view and update profile; admin can manage any user
- **Swagger / OpenAPI 3** — interactive docs at `/api/v1/swagger-ui.html`
- **Docker** — single `docker compose up` spins up MySQL + backend + frontend
- **CI/CD** — GitHub Actions pipeline: test → build → push GHCR images → deploy

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|-------------------------------------------------|
| Language    | Java 17, JavaScript (ES2022)                    |
| Framework   | Spring Boot 3.2.3, React 18                     |
| Security    | Spring Security 6, JWT (jjwt 0.12.3)           |
| Persistence | Spring Data JPA, Hibernate, MySQL 8             |
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
│                                 │ MySQL 8 (3306)          │   │
│                                 │ Named volume for data   │   │
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
| POST   | `/auth/login`          | —    | Login, returns JWT   |

### Posts

| Method | Path                          | Auth     | Description                       |
|--------|-------------------------------|----------|-----------------------------------|
| GET    | `/posts`                      | —        | List posts (paginated)            |
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
export JWT_EXPIRATION_MS=86400000
./mvnw spring-boot:run
```

The API starts on **`http://localhost:8080`**.

> A default admin account is seeded on first startup:
> - Username: `admin` | Password: `Admin@123`

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
| `JWT_SECRET`         | **Yes**  | *(none)*       | ≥64-char random string for HS512       |
| `JWT_EXPIRATION_MS`  | No       | 86400000       | Token lifetime in milliseconds (24 h) |

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

## Future Improvements

- [ ] Refresh token endpoint and token blacklist (Redis)
- [ ] Email verification on registration (Spring Mail)
- [ ] Image uploads for post cover photos (AWS S3 / MinIO)
- [ ] Tag system for posts (many-to-many)
- [ ] Post view-count tracking
- [ ] WebSocket notifications for new comments
- [ ] Pagination cursor-based (keyset) for large datasets
- [ ] Rate limiting per IP and per authenticated user (Bucket4j)
- [ ] Kubernetes Helm chart for cloud deployment
- [ ] React Native mobile client
