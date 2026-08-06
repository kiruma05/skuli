# End-to-end smoke test

Verifies that a **real Keycloak-issued token** is accepted by the Spring backend and drives the
tenant-scoped REST API — the integration the frontend (Phase 4) relies on.

## What it checks

With a real admin access token from Keycloak:

- unauthenticated `GET /api/v1/subjects` → **401**
- admin `GET /api/v1/subjects` → **200** (returns the dev-seeded subjects)
- admin `POST /api/v1/subjects` → **201**, and the list count increments

## Option A — self-contained (bundled Keycloak)

Brings up Postgres + Keycloak (configured from `keycloak/realm-skuli.json`: realm `skuli`, client
`school-app`, an `admin1`/`admin123` user, and a hardcoded `tenant_id=default` claim) + the backend
with the dev seed, then asserts the above.

```sh
./scripts/e2e-smoke.sh          # runs, then leaves the stack up
KEEP=0 ./scripts/e2e-smoke.sh   # runs, then tears the stack down
```

Requires host port **8080 free** — stop any externally-run Keycloak first, since this bundles its
own on 8080. `KC_HOSTNAME` pins the issuer to `http://host.docker.internal:8080` so the token `iss`
matches the backend's configured issuer regardless of how Keycloak is reached.

## Option B — against an already-running Keycloak

If you already run Keycloak separately (realm `skuli`, your own client), point a freshly-built
backend at it and mint a token via the direct-access-grant (password) flow. This was used to verify
the current code: a throwaway admin user was created in the realm via the master admin API, the
smoke asserted the three checks above (all passed), then the user was deleted and the client's
Direct Access Grants setting restored. The token's issuer was
`http://host.docker.internal:8080/realms/skuli` and admin-only endpoints returned 200, confirming
role mapping (`realm_access.roles` → `ROLE_*`) works with real tokens.

Note: the password grant needs the client's **Direct Access Grants** enabled, and the test user
needs a complete profile (email + first/last name) or Keycloak's *Verify Profile* action blocks
login with "Account is not fully set up".

## Manual browser check (frontend)

```sh
docker compose -f docker-compose.yml -f docker-compose.frontend.yml up -d --build
```

Then open http://localhost:3000, sign in as an admin, and load **/list/subjects** (or
**/list/classes**) — these are served by the backend. Requires `127.0.0.1 host.docker.internal` in
the host's `/etc/hosts` so the browser and containers share one Keycloak hostname.
