# SwarmForge - Security Audit Report

## 1. Authentication & Access Control
- **gRPC Security:** Secured via `JwtServerInterceptor`. Requests are authenticated using HMAC-SHA256 JWT tokens.
- **Role-Based Access Control (RBAC):** Differentiates `ADMIN` (full control over server parameters, simulation lifecycle, and user management) and `USER` (read/stream access).

## 2. Input Validation & Deserialization Security
- **JSON Security:** PolyMorphic Jackson deserialization attack vectors are mitigated by disabling default typing and using explicit class mapping in `SpeciesPresetManager` and `CustomSpecies`.
- **SQL Injection Prevention:** Database interactions in `swarmforge-server` use parameterized queries / PreparedStatements with H2/PostgreSQL.

## 3. Network Hygiene & Production Recommendations
- **TLS/SSL Encryption:** Recommended configuration for gRPC endpoint binding in production environments using TLS certificates.
- **Secret Management:** Secrets (`SWARMFORGE_JWT_SECRET`, admin passwords) are loaded via environment variables rather than hardcoded credentials.
