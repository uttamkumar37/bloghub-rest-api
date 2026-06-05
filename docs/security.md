# Security

## Purpose
Document BlogHub's production security model: access tokens, refresh token rotation, logout blacklist, password policy, account lockout, CORS, headers, and safe admin bootstrap.

## Practical Usage Steps
1. Use `/auth/login` to receive an access token and refresh token.
2. Send `Authorization: Bearer <accessToken>` to protected APIs.
3. Use `/auth/refresh` before access-token expiry; refresh tokens rotate on every use.
4. Call `/auth/logout` with the access token and refresh token to blacklist/revoke both.
5. Configure `JWT_SECRET`, Redis, CORS origins, and admin bootstrap env vars per environment.

## Implementation Notes
- Access tokens are short-lived JWTs; refresh tokens are opaque random tokens stored as SHA-256 hashes.
- Redis stores access-token blacklist keys until the original JWT expiry.
- Auth endpoints are rate-limited by IP using Redis with in-memory fallback.
- Passwords require length plus uppercase, lowercase, number, and special character.
- Account lockout triggers after repeated failed login attempts.
- Admin bootstrap is disabled unless `APP_BOOTSTRAP_ADMIN_ENABLED=true` and a strong password is supplied.

## Operational Notes
- Monitor `bloghub.cache.requests`, Redis latency, auth 401/403/429 rates, and failed-login spikes.
- Rotate `JWT_SECRET` with a planned maintenance window unless dual-signing support is added.
- Keep `/actuator/prometheus` behind network controls in production.

## Failure Modes
- Redis unavailable: blacklist and rate-limit services fall back in memory, which is safe for one instance but not cluster-wide.
- Refresh token reuse: the reused token is rejected because the previous token is marked revoked.
- Clock skew: short access-token TTLs require synchronized server clocks.

## Rollback Notes
Set auth rate limits higher or temporarily disable Redis-dependent protections only during a controlled incident. Do not re-enable hardcoded admin credentials.

## Practical Example
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usernameOrEmail":"alice","password":"StrongPass@123"}'
```

## Interview Questions
- Why store refresh tokens as hashes instead of raw tokens?
- How does refresh token rotation detect token replay?
- What changes when token blacklist moves from single instance to Redis?

## Common Mistakes
- Using long-lived JWT access tokens without revocation strategy.
- Seeding an admin password in source control.
- Allowing wildcard CORS with credentials.

## Self-Check
- [ ] Access token TTL is short and configurable.
- [ ] Refresh token rotation is tested.
- [ ] Logout blacklists the access token and revokes refresh token.
- [ ] CORS origins are environment-specific.

