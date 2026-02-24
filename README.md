## BlogHub – Scalable RESTful Blogging Platform (REST API)

### What you get
- **JWT authentication** (register/login) with **BCrypt** password hashing
- **Role-based access**: `ROLE_ADMIN`, `ROLE_USER`
- **Posts**: CRUD, pagination, filter by category, keyword search
- **Comments**: add, list-by-post, delete (admin or owner)
- **Categories**: CRUD (admin-only for write ops)
- **DTOs + validation**, standardized JSON responses, global exception handling
- **OpenAPI/Swagger UI**

### Tech stack
- Java 17
- Spring Boot, Spring Security, Spring Data JPA (Hibernate)
- JWT (JJWT)
- PostgreSQL or MySQL
- Maven

### Run locally
#### 1) Prerequisites
- Install **Java 17+**
- Install **Maven** (or add Maven Wrapper later if you prefer)
- Start a database (PostgreSQL recommended)

#### 2) Configure environment
Create `src/main/resources/application-local.yml` (ignored by git) and set:

```yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bloghub
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        show_sql: false

bloghub:
  security:
    jwt:
      secret: "CHANGE_ME_TO_A_LONG_RANDOM_SECRET_AT_LEAST_256_BITS"
      expiration-minutes: 120
```

#### 3) Start the app
```bash
mvn spring-boot:run
```

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`

Default admin user (auto-created at startup):
- **email**: `admin@bloghub.local`
- **password**: `Admin@123`

### API overview
Base URL: `/api/v1`

- **Auth**
  - `POST /auth/register`
  - `POST /auth/login`
  - `GET /auth/me`
- **Categories**
  - `GET /categories`
  - `GET /categories/{id}`
  - `POST /categories` (admin)
  - `PUT /categories/{id}` (admin)
  - `DELETE /categories/{id}` (admin)
- **Posts**
  - `GET /posts` (pagination + optional `categoryId`, `keyword`)
  - `GET /posts/{id}`
  - `POST /posts` (auth)
  - `PUT /posts/{id}` (owner/admin)
  - `DELETE /posts/{id}` (owner/admin)
- **Comments**
  - `GET /posts/{postId}/comments`
  - `POST /posts/{postId}/comments` (auth)
  - `DELETE /comments/{commentId}` (owner/admin)
- **Admin**
  - `GET /admin/users` (admin)

### Project structure (backend)

```text
src/
  main/
    java/com/bloghub
      BloghubRestApiApplication.java   # Spring Boot entrypoint
      config/                          # Config, OpenAPI, properties binding, data init
      domain/                          # JPA entities (User, Role, Post, Comment, Category)
      repository/                      # Spring Data JPA repositories
      security/                        # JWT, filters, UserDetailsService, security config
      service/                         # Business logic (auth, posts, comments, categories, admin)
      util/                            # Helpers (e.g. slug generation)
      web/                             # Controllers, exception handler, API paths
        dto/                           # DTOs for requests/responses
    resources/
      application.yml                  # Base config (profile, JSON, JPA hints)
      application-*.yml                # Per-environment overrides (you create local/prod)
```

### Configuration & profiles

- **Default profile**: `local` (set in `application.yml`).
- **Local overrides**:
  - Create `application-local.yml` with DB URL, credentials and JWT secret (see sample above).
- **Environment variables (optional)**:
  - `BLOGHUB_JWT_SECRET` – overrides `bloghub.security.jwt.secret`
  - `BLOGHUB_JWT_EXP_MINUTES` – overrides token expiration (minutes)

#### Switch to MySQL

Example `application-local.yml` for MySQL:

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bloghub?useSSL=false&serverTimezone=UTC
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

### Authentication & authorization

- **Register**: `POST /api/v1/auth/register`
  - Body: `{ "email", "password", "displayName" }`
  - Creates a `ROLE_USER` with hashed password (BCrypt).
- **Login**: `POST /api/v1/auth/login`
  - Body: `{ "email", "password" }`
  - Returns `{ accessToken, tokenType, user }`.
- **Current user**: `GET /api/v1/auth/me`
  - Requires `Authorization: Bearer <token>`.

#### Roles

- `ROLE_ADMIN`
  - Manage users list, categories, and moderate posts/comments.
- `ROLE_USER`
  - Manage own posts and comments.

#### Secured endpoints (high level)

- Public:
  - `POST /auth/register`, `POST /auth/login`
  - `GET /posts`, `GET /posts/{id}`, `GET /categories`, `GET /categories/{id}`
  - `GET /posts/{postId}/comments`
- Authenticated:
  - `POST /posts`, `PUT /posts/{id}`, `DELETE /posts/{id}`
  - `POST /posts/{postId}/comments`, `DELETE /comments/{commentId}`
  - `GET /auth/me`
- Admin-only:
  - `POST /categories`, `PUT /categories/{id}`, `DELETE /categories/{id}`
  - `GET /admin/users`

### Pagination & filtering

- **Posts listing**: `GET /api/v1/posts`
  - Query parameters:
    - `page` – 0-based page index, default `0`
    - `size` – page size (1–100), default `10`
    - `categoryId` – optional filter by category
    - `keyword` – optional text search in title/content (contains, case-insensitive)
  - Response: `ApiResponse<PageResponse<PostDto>>`:
    - `content` – list of posts
    - `page`, `size`, `totalElements`, `totalPages`

### Standard response format

- **Success**:

```json
{
  "success": true,
  "message": "Post created",
  "data": {
    "...": "resource specific fields"
  }
}
```

- **Error**:

```json
{
  "timestamp": "2026-02-25T10:15:30.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/posts",
  "validationErrors": {
    "title": "must not be blank"
  }
}
```

### Database model (high level)

- **User**
  - `id`, `email` (unique), `passwordHash`, `displayName`, `role_id`, `createdAt`, `updatedAt`
  - One `User` → many `Post`, many `Comment`.
- **Role**
  - `id`, `name` (`ROLE_ADMIN` / `ROLE_USER`)
  - One `Role` → many `User`.
- **Category**
  - `id`, `name` (unique), `slug` (unique), `createdAt`, `updatedAt`
  - One `Category` → many `Post`.
- **Post**
  - `id`, `title`, `content`, `imageUrl`, `category_id`, `author_id`, `createdAt`, `updatedAt`
  - One `Post` → many `Comment`.
- **Comment**
  - `id`, `content`, `post_id`, `author_id`, `createdAt`, `updatedAt`

Indexes are defined on frequently queried columns (email, role, category, author, createdAt, etc.) for performance.

### Testing

- **Unit / integration tests**
  - You can add tests under `src/test/java/com/bloghub/...` targeting:
    - `AuthService` (registration/login)
    - `PostService` and `CommentService` (ownership & admin rules)
    - Controllers using `@WebMvcTest` or full `@SpringBootTest`.
- **Postman**
  - Import `postman_collection.json` in Postman.
  - Use the login response `accessToken` to set `{{token}}` environment variable for authorized calls.

### Deploying to production (high level)

- Use a dedicated profile (e.g. `prod`) via:
  - JVM arg: `-Dspring.profiles.active=prod`
  - Or env var: `SPRING_PROFILES_ACTIVE=prod`
- Configure:
  - Managed database (PostgreSQL/MySQL) connection.
  - Strong `BLOGHUB_JWT_SECRET`.
  - Proper logging level and rotation.
- For Docker:
  - Build a jar: `mvn -DskipTests package`
  - Use a simple Dockerfile (Temurin JDK 17) to run `java -jar bloghub-rest-api-0.0.1-SNAPSHOT.jar`.

