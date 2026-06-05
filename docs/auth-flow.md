# Auth Flow

## Purpose
Explain the end-to-end authentication lifecycle for clients, backend code, tests, and interviews.

## Practical Usage Steps
1. Register with a strong password.
2. Login to receive `accessToken`, `refreshToken`, and expiry metadata.
3. Use the access token for APIs until expiry.
4. Refresh using the refresh token; store the newly returned refresh token.
5. Logout to revoke the current refresh token and blacklist the access token.

## Implementation Notes
- `AuthController` exposes `/auth/register`, `/auth/login`, `/auth/refresh`, and `/auth/logout`.
- `RefreshTokenService` issues and rotates opaque refresh tokens.
- `JwtAuthenticationFilter` rejects blacklisted access tokens before setting the security context.
- Account lockout is tracked on `users.failed_login_attempts` and `users.locked_until`.

## Flow Diagram
```text
Client -> /auth/login -> AuthService -> JWT access token
                              |
                              v
                       RefreshTokenService
                              |
                              v
                        refresh_tokens table

Client -> /auth/refresh -> validate old refresh -> revoke old -> issue new access + refresh
Client -> /auth/logout  -> Redis blacklist access token + revoke refresh token
```

## Failure Modes
- Expired access token: client calls `/auth/refresh`.
- Expired refresh token: client must login again.
- Reused refresh token: backend rejects and logs suspicious behavior.
- Rate limit exceeded: backend returns HTTP 429.

## Monitoring Notes
- Track login success/failure rate, refresh failures, logout count, and 429 count.
- Alert on sudden failed-login spikes or refresh-token replay errors.

## Rollback Notes
Keep `/auth/login` response backward compatible by leaving `accessToken` and `tokenType` unchanged. Clients can adopt refresh tokens incrementally.

## Practical Example
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "opaque-random-token",
  "tokenType": "Bearer",
  "accessTokenExpiresInMs": 900000
}
```

## Interview Questions
- Why not store JWTs in the database?
- Why rotate refresh tokens on every refresh?
- How do you handle logout for stateless JWT APIs?

## Common Mistakes
- Reusing the same refresh token forever.
- Returning refresh tokens in URLs.
- Not rate-limiting login and registration.

## Self-Check
- [ ] Refresh token is opaque and random.
- [ ] Refresh token is stored hashed.
- [ ] Logout has tests for blacklist/revocation.
- [ ] Client can recover from access-token expiry.

