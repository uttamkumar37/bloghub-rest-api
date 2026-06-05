# File Upload

## Purpose
Document post cover image upload design for local development and S3-compatible production storage.

## Practical Usage Steps
1. Authenticate with a bearer token.
2. Upload to `/files/post-cover` as `multipart/form-data`.
3. Store returned `publicUrl` on a post in a later post-cover association endpoint.
4. Use MinIO locally or S3-compatible object storage in production.

## Implementation Notes
- Current service stores local files under `UPLOAD_LOCAL_DIR`.
- Allowed types: JPEG, PNG, WebP.
- Default max size: 5 MB.
- Filenames are generated with UUIDs; user-supplied names are not trusted.
- Virus scanning is a documented placeholder before production launch.

## Failure Modes
- Oversized file: HTTP 400.
- Unsupported content type: HTTP 400.
- Disk full or permission issue: HTTP 500.
- Public URL mismatch behind CDN/proxy: configure `UPLOAD_PUBLIC_BASE_URL`.

## Monitoring Notes
- Upload success/error count.
- Storage volume usage.
- File size distribution.
- Malware scan failures once scanner is integrated.

## Rollback Notes
Disable the upload endpoint at the gateway or feature flag. Existing local files remain readable through `/uploads/**`.

## Practical Example
```bash
curl -X POST http://localhost:8080/api/v1/files/post-cover \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@cover.webp;type=image/webp"
```

## Interview Questions
- How do you prevent path traversal in uploads?
- Why use object storage instead of database BLOBs?
- How would you add virus scanning asynchronously?

## Common Mistakes
- Trusting original filenames.
- Serving private files from a public path.
- Missing file size and content-type checks.

## Self-Check
- [ ] Size limit is enforced.
- [ ] Content types are allow-listed.
- [ ] Filenames are server-generated.
- [ ] Production S3/MinIO path is documented.

