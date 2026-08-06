#!/bin/sh
# End-to-end smoke test: brings up Postgres + Keycloak + the Spring backend, obtains a REAL
# Keycloak access token, and asserts the backend's auth + tenant + REST path works.
#
#   ./scripts/e2e-smoke.sh          # run the smoke test, then leave the stack running
#   KEEP=0 ./scripts/e2e-smoke.sh   # tear the stack down afterwards
#
# All HTTP calls run from a throwaway curl container on the compose network, so no host hostname
# entries or published-port juggling are needed.
set -e
cd "$(dirname "$0")/.."

COMPOSE="docker compose -f docker-compose.yml -f docker-compose.backend.yml -f docker-compose.e2e.yml"
NET=skuli_default
KEEP="${KEEP:-1}"
FAILURES=0

dcurl() { docker run --rm --network "$NET" curlimages/curl:8.11.1 -s "$@"; }

check() { # desc  actual  expected
  if [ "$2" = "$3" ]; then
    echo "  PASS: $1 ($2)"
  else
    echo "  FAIL: $1 (got $2, expected $3)"
    FAILURES=$((FAILURES + 1))
  fi
}

wait_for() { # desc url
  printf 'Waiting for %s ' "$1"
  i=0
  while [ "$i" -lt 90 ]; do
    if dcurl -f -o /dev/null "$2" 2>/dev/null; then echo "ready"; return 0; fi
    printf '.'; i=$((i + 1)); sleep 2
  done
  echo " TIMEOUT"; return 1
}

token_for() { # username password
  dcurl -X POST "http://keycloak:8080/realms/skuli/protocol/openid-connect/token" \
    -d grant_type=password -d client_id=school-app -d client_secret=school-app-secret \
    -d "username=$1" -d "password=$2" \
    | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
}

status() { # token(may be empty)  method  path  [body]
  if [ -n "$1" ]; then AUTH="-H Authorization:Bearer\ $1"; else AUTH=""; fi
  if [ -n "$4" ]; then
    dcurl -o /dev/null -w '%{http_code}' -X "$2" $AUTH \
      -H "Content-Type: application/json" -d "$4" "http://backend:8080$3"
  else
    dcurl -o /dev/null -w '%{http_code}' -X "$2" $AUTH "http://backend:8080$3"
  fi
}

echo "==> Building and starting Postgres + Keycloak + backend ..."
$COMPOSE up -d --build

wait_for "Keycloak realm" "http://keycloak:8080/realms/skuli"
wait_for "backend health" "http://backend:8080/actuator/health"

echo "==> Acquiring tokens"
ADMIN_TOKEN=$(token_for admin1 admin123)
TEACHER_TOKEN=$(token_for teacher1 teacher123)
[ -n "$ADMIN_TOKEN" ] && echo "  admin token acquired" || { echo "  FAIL: no admin token"; FAILURES=$((FAILURES + 1)); }

echo "==> Asserting backend behavior"
check "unauthenticated GET /subjects -> 401" "$(status '' GET /api/v1/subjects)" "401"
check "teacher GET /subjects (admin-only) -> 403" "$(status "$TEACHER_TOKEN" GET /api/v1/subjects)" "403"
check "admin GET /subjects -> 200" "$(status "$ADMIN_TOKEN" GET /api/v1/subjects)" "200"

BEFORE=$(dcurl -H "Authorization: Bearer $ADMIN_TOKEN" "http://backend:8080/api/v1/subjects" \
  | sed -n 's/.*"totalElements":\([0-9]*\).*/\1/p')
echo "  seeded subjects: ${BEFORE:-?}"

check "admin POST /subjects -> 201" \
  "$(status "$ADMIN_TOKEN" POST /api/v1/subjects '{"name":"E2E Smoke Subject"}')" "201"

AFTER=$(dcurl -H "Authorization: Bearer $ADMIN_TOKEN" "http://backend:8080/api/v1/subjects" \
  | sed -n 's/.*"totalElements":\([0-9]*\).*/\1/p')
check "subject count incremented" "$AFTER" "$((BEFORE + 1))"

echo ""
if [ "$FAILURES" -eq 0 ]; then
  echo "E2E SMOKE: PASS"
else
  echo "E2E SMOKE: FAIL ($FAILURES check(s) failed)"
fi

if [ "$KEEP" = "1" ]; then
  echo ""
  echo "Stack left running. Backend: http://localhost:8081  Keycloak: http://localhost:8080"
  echo "Add the frontend:  docker compose -f docker-compose.yml -f docker-compose.frontend.yml up -d --build"
  echo "Stop everything:   $COMPOSE down"
else
  $COMPOSE down
fi

exit "$FAILURES"
