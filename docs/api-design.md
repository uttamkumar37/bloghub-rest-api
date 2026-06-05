# API Design

## Purpose
Document API conventions for response shape, errors, validation, versioning, idempotency, and OpenAPI.

## Practical Usage Steps
1. Use `/api/v1` as the versioned base path.
2. Return standard success wrappers for command-style responses.
3. Return standard error payloads from `GlobalExceptionHandler`.
4. Use `Idempotency-Key` on selected write endpoints.
5. Keep OpenAPI annotations aligned with controller behavior.

## Implementation Notes
- `ApiResponse` standardizes command responses.
- `ErrorResponse` contains timestamp, status, error, message, path, and correlation ID.
- Validation uses Jakarta Bean Validation DTO annotations.
- `POST /posts` supports optional `Idempotency-Key`.
- Backward compatibility is preserved for core post/auth payloads.

## Failure Modes
- Validation error: HTTP 400 with field map.
- Unauthorized: HTTP 401.
- Forbidden: HTTP 403.
- Duplicate idempotency key: HTTP 409.
- Rate limit exceeded: HTTP 429.

## Monitoring Notes
- Track 4xx by error type.
- Track 409 idempotency rejections.
- Track validation failures by endpoint for API usability.

## Rollback Notes
Version breaking response changes under `/api/v2`; do not mutate `/api/v1` semantics silently.

## Practical Example
```json
{
  "timestamp": "2026-06-06T01:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Check 'validationErrors' for details.",
  "path": "/api/v1/posts",
  "correlationId": "req-123"
}
```

## Interview Questions
- How do you version REST APIs?
- When should a write endpoint be idempotent?
- What should a standard error response include?

## Common Mistakes
- Returning inconsistent error structures.
- Hiding validation details from clients.
- Adding breaking fields/removing fields in v1 without migration path.

## Self-Check
- [ ] Errors include correlation ID.
- [ ] Validation errors include field-level details.
- [ ] Idempotency is opt-in and documented.
- [ ] OpenAPI docs match controllers.

