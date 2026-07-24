# School Platform — Backend Architecture & Migration Plan

Migrate the backend of `full-stack-school-main` (Next.js server actions + Prisma + NextAuth/Keycloak) to a **Spring Boot modular monolith**, with **AI isolated in a separate Python/FastAPI service**. Next.js stays as the frontend. This document is a plan only — no code is generated here.

---

## 0. Architectural position (what changed and why)

Three decisions frame everything below.

**1. Spring Boot stays the core.** Confirmed. The system's centre of gravity is transactional CRUD, role-based access, auditability, and future government/payment/SMS integrations. That is Spring's strongest ground.

**2. AI lives outside Spring, in Python.** Adopted. Forcing OpenCV, TensorFlow, or an LLM/RAG stack into the JVM buys nothing and costs a lot. The AI service owns models and inference; Spring owns records and rules.

**3. Modular monolith, not microservices.** Adopted, with a constraint: modules are **Maven modules with enforced boundaries**, deployed as one JAR. Splitting into separately deployed services before there is real load, a real team split, or a real scaling asymmetry buys distributed-systems pain and no benefit.

### Where I disagree with the review

Two items in the recommendation are **correct as destinations, wrong as starting points**:

- **API Gateway on day one — deferred.** With two backends and one frontend, a gateway is an extra hop, an extra deploy, and an extra place for auth to break. Next.js already has a server side that can act as the client-facing edge. Introduce a real gateway when there is a third consumer (mobile app, partner integration) or an external API contract to police. Design *for* it now (see §9) — don't build it now.
- **Kafka / Kubernetes — deferred, but designed for.** Domain events get published to an in-process Spring event bus behind an interface. When Kafka arrives, the publisher implementation changes and the call sites do not. Same for k8s: containerise everything, keep config in env vars, keep the app stateless — that *is* the k8s prerequisite work, and it pays off on plain Docker Compose too.

**One addition the review did not cover: multi-tenancy.** "Multi-school SaaS" is on the roadmap. Tenancy is the single hardest thing to retrofit — it touches every table, every query, and every security check. See §10; the cheap version of this decision must be made **now**, in `V1__init.sql`.

### Target topology

```
                     Next.js (frontend + BFF edge)
                                │
                                │  JWT (Keycloak)
                                ▼
                  ┌──────────────────────────┐
                  │   Spring Boot (modular)  │
                  │  auth · student · staff  │
                  │  academics · finance     │
                  │  notification · report   │
                  └──────┬────────────┬──────┘
                         │            │  S2S JWT (client credentials)
                    PostgreSQL        ▼
                         │     ┌──────────────────┐
                     Keycloak  │ AI Service       │
                               │ Python / FastAPI │
                               │ models + vectors │
                               └──────────────────┘
                                        │
                              (own store: pgvector / S3 models)

              Observability: Actuator + Micrometer → Prometheus → Grafana
```

Spring Boot is the **only** writer to the operational database. The AI service never writes school records — it returns predictions and text; Spring decides what to persist.

---

## 1. Target stack

| Concern | Current (Next.js) | Target |
|---|---|---|
| Runtime | Node / Next 14 | Java 21, Spring Boot 3.3.x, Maven multi-module |
| ORM | Prisma | Spring Data JPA + Hibernate |
| DB | PostgreSQL 15 | PostgreSQL 15 (unchanged) |
| Migrations | Prisma migrate | Flyway |
| Validation | Zod | Jakarta Bean Validation |
| AuthN | NextAuth (Keycloak OIDC) | Spring Security OAuth2 **Resource Server** (JWT) |
| AuthZ | `routeAccessMap` + middleware | `@PreAuthorize` + `SecurityFilterChain` |
| User provisioning | `keycloak.ts` (fetch) | `keycloak-admin-client` in `KeycloakService` |
| API shape | Server actions (RPC-ish) | REST (`/api/**`), JSON |
| **AI** | — | **Python 3.12, FastAPI, Uvicorn** |
| **Observability** | — | Actuator, Micrometer, structured JSON logs, correlation IDs |

**Key auth shift:** Keycloak remains the identity provider. Next.js still runs the OIDC login flow and holds the token; Spring Boot only **validates** JWTs. Login does not move to Spring.

**Why FastAPI over Django:** the AI service serves inference endpoints — no admin UI, no ORM, no templating. FastAPI gives async I/O, Pydantic schemas, and OpenAPI generation with none of Django's weight. Agreed with the review.

---

## 2. Project layout — modular monolith

```
school-platform/
├── pom.xml                          # parent, dependency management
├── common-library/                  # shared kernel — NO domain logic
│   ├── error/                       # ApiError, exception base types
│   ├── security/                    # JwtAuthConverter, @CurrentUser, TenantContext
│   ├── event/                       # DomainEvent, EventPublisher (interface)
│   └── util/                        # pagination, audit primitives
├── module-auth/                     # Keycloak provisioning, role mapping
├── module-student/                  # Student, Parent, enrolment, capacity rules
├── module-staff/                    # Teacher, Admin, subject assignment
├── module-academics/                # Grade, Class, Subject, Lesson, Exam,
│                                    #   Assignment, Result, Attendance
├── module-finance/                  # (phase 2) fees, invoices, payments
├── module-communication/            # Event, Announcement, SMS/email dispatch
├── module-reporting/                # report cards, aggregates, exports
├── module-ai-client/                # typed HTTP client for the AI service
├── app/                             # ← the ONLY deployable: main class,
│                                    #   application.yml, Flyway migrations
└── ai-service/                      # separate repo-in-repo, Python
    ├── app/
    │   ├── main.py
    │   ├── routers/                 # predict, risk, report, chat, vision
    │   ├── models/                  # loaders, versioning
    │   ├── schemas/                 # Pydantic request/response
    │   └── core/                    # config, JWT verification, logging
    ├── tests/
    ├── pyproject.toml
    └── Dockerfile
```

### Boundary rules (the part that makes this work)

A modular monolith without enforcement is a monolith with extra folders. Enforce mechanically:

1. **Modules never import each other's `internal` packages.** Each module exposes `api/` (DTOs + service interfaces); everything else is `internal/`.
2. **Cross-module calls go through the exposed interface or a domain event** — never a direct repository call and never a JPA relationship across a module boundary. Where a foreign key crosses modules (`Student.classId`), the entity holds the **ID**, not the object; the owning module resolves it.
3. **Enforce in CI with ArchUnit.** A failing test on an illegal import is worth more than any architecture document — including this one.
4. **One schema, one Flyway history**, in `app/`. Table ownership is documented per module; only the owning module writes its tables.

Rule 2 is the one that makes a future service extraction cheap. Rule 3 is the one that makes rule 2 survive contact with a deadline.

---

## 3. Data model → JPA entities

Port all 14 Prisma models. Notes and gotchas:

- **Enums** `UserSex` (MALE/FEMALE), `Day` (MON–FRI) → Java enums, `@Enumerated(EnumType.STRING)`.
- **String PKs** for `Admin`, `Student`, `Teacher`, `Parent` (id == username) → `@Id String id`, not generated.
- **Int autoincrement PKs** (`Grade`, `Class`, `Subject`, `Lesson`, `Exam`, `Assignment`, `Result`, `Attendance`, `Event`, `Announcement`) → `@GeneratedValue(IDENTITY)`.
- **Relationships:**
  - `Student` → `@ManyToOne` Parent, Class, Grade; `@OneToMany` Attendance, Result.
  - `Teacher` ↔ `Subject` `@ManyToMany`; `Teacher` `@OneToMany` Lesson; `@OneToMany` Class (supervisor, nullable).
  - `Class` → `@ManyToOne` Grade, optional supervisor Teacher; `@OneToMany` Lesson/Student/Event/Announcement.
  - `Lesson` → `@ManyToOne` Subject/Class/Teacher; `@OneToMany` Exam/Assignment/Attendance.
  - `Result` → nullable `@ManyToOne` Exam **or** Assignment + required Student.
- **`createdAt`** `@default(now())` → JPA auditing (`@CreatedDate`/`@CreatedBy`), not just `@CreationTimestamp` — `@CreatedBy` is the foundation of the audit trail.
- **Uniqueness** (`username`, `email`, `phone`, `name`, `level`) → `@Column(unique = true)` + DB constraints in Flyway.
- Prisma implicit M:N (`_SubjectToTeacher`) → explicit `@JoinTable(name = "subject_teachers")`.
- **Cross-module FKs** (e.g. `Student.classId` where `Class` is in `module-academics`) → keep the **DB-level** foreign key in Flyway for integrity; drop the **object** reference in JPA. Boundary integrity in the database, boundary discipline in the code.

**Schema ownership:** `spring.jpa.hibernate.ddl-auto=validate`, schema owned by **Flyway `V1__init.sql`** translated from `prisma/migrations`. Reuse the existing Postgres container/credentials from `docker-compose.yml`.

---

## 4. Endpoint mapping (server actions → REST)

Write actions live in `src/lib/actions.ts`; reads are inline Prisma calls in `(dashboard)/list/**`.

| Resource | Endpoints | Module | Source today |
|---|---|---|---|
| Subjects | `GET /api/subjects`, `POST`, `PUT /{id}`, `DELETE /{id}` | academics | `createSubject`/… |
| Classes | `GET /api/classes` (+`?page`), `POST`, `PUT/{id}`, `DELETE/{id}` | academics | `createClass`/… |
| Teachers | `GET`, `GET /{id}`, `POST`, `PUT/{id}`, `DELETE/{id}` | staff | `createTeacher` (+ Keycloak) |
| Students | `GET`, `GET /{id}`, `POST`, `PUT/{id}`, `DELETE/{id}` | student | `createStudent` (+ capacity check) |
| Exams | `GET`, `POST`, `PUT/{id}`, `DELETE/{id}` | academics | `createExam`/… |
| Parents | `GET`, `POST`, `PUT/{id}`, `DELETE/{id}` | student | (data.ts placeholder → real CRUD) |
| Lessons / Assignments / Results / Attendance | standard CRUD | academics | list pages / seed |
| Events / Announcements | standard CRUD | communication | list pages / seed |

**Pagination:** replace `ITEM_PER_PAGE = 10` with Spring `Pageable` (`?page=0&size=10`) → `Page<T>`. Keep `?search=` via `Specification`, mirroring `TableSearch`.

**Response contract:** current actions return `{ success, error }`. Move to HTTP status codes (200/201/400/404/409) + an RFC 7807 `application/problem+json` body from `@RestControllerAdvice`. Problem Details is a standard — worth adopting now, because government integration reviews ask about exactly this kind of thing.

**API versioning:** prefix everything `/api/v1/**` from the first commit. It costs nothing today and is the difference between a clean and a painful breaking change later.

---

## 5. Business logic to preserve

1. **Student create capacity check** — reject when `class.capacity == class._count.students` → `studentRepository.countByClassId`.
2. **Keycloak-then-DB with rollback** — create the Keycloak user first, then the DB row; on DB failure delete the Keycloak user. Keycloak is not transactional with the DB, so keep the compensating delete in `catch`. **Improve on today:** log every compensation attempt and its outcome to an `provisioning_audit` table, so a failed compensation is visible instead of silent.
3. **Delete order** — DB delete, then Keycloak delete.
4. **Update propagates** username/password/email to Keycloak via Admin API.
5. **`id == username` invariant** for teachers and students.

---

## 6. Keycloak integration

Replace `keycloak.ts` fetch calls with **`org.keycloak:keycloak-admin-client`**, wrapped in `KeycloakService` inside `module-auth`.

- Client-credentials (service account) grant — same `school-app` client; needs `manage-users`, `view-users`, `query-users` realm-management roles.
- Methods: `createUser`, `updateUser`, `deleteUser`, `assignRealmRole`, `findUserId(username)`.
- Config via `@ConfigurationProperties(prefix="keycloak")` from `AUTH_KEYCLOAK_ISSUER`, `AUTH_KEYCLOAK_ID`, `AUTH_KEYCLOAK_SECRET`.

**Resource-server side:** `spring.security.oauth2.resourceserver.jwt.issuer-uri = ${AUTH_KEYCLOAK_ISSUER}`. A `JwtAuthenticationConverter` reads `realm_access.roles` (what `auth.ts` decodes today) → `ROLE_admin`, `ROLE_teacher`, `ROLE_student`, `ROLE_parent`.

**Service-to-service:** register a second Keycloak client `school-ai` with client-credentials grant. Spring obtains a token and presents it to the AI service; the AI service validates it against the same issuer. Same identity provider, one trust model, no shared secrets or IP allowlists.

---

## 7. Authorization

Translate `settings.ts` `routeAccessMap` to security rules:

- Coarse rules in `SecurityConfig` (`requestMatchers("/api/v1/subjects/**").hasRole("admin")`).
- Fine-grained rules via `@PreAuthorize` on service methods.

| Route pattern | Roles |
|---|---|
| `/api/v1/subjects/**` | admin |
| `/api/v1/teachers`, `/students`, `/parents`, `/classes` | admin, teacher |
| `/api/v1/exams`, `/assignments`, `/results`, `/attendance`, `/events`, `/announcements` | admin, teacher, student, parent |

**Row-level access is the real gap.** Coarse role checks let any parent read any student's results. Before launch, every read of student-scoped data must be filtered by relationship: parent → own children, student → self, teacher → own classes/lessons. Implement as a Hibernate filter or a `@PreAuthorize` + Specification pair; do **not** leave it to controller-by-controller discipline.

**Decision to confirm:** the commented-out teacher-scoped exam authorization in `actions.ts` — re-enable it as part of the above.

**CORS:** allow the Next.js origin (`http://localhost:3000` in dev), credentials off, explicit method/header allowlist.

---

## 8. AI service (Python / FastAPI)

### Endpoints

| Endpoint | Purpose | Maturity |
|---|---|---|
| `POST /v1/predict/performance` | predicted grade / trend per student | phase 3 |
| `POST /v1/predict/risk` | dropout / at-risk score + contributing factors | phase 3 |
| `POST /v1/generate/report-comment` | narrative comment from results + attendance | phase 3 |
| `POST /v1/assistant/ask` | parent/teacher Q&A over school data | phase 4 |
| `POST /v1/vision/attendance` | face-recognition attendance from a frame | phase 5 |
| `GET /health`, `GET /v1/models` | liveness + model versions in use | phase 3 |

### Contract rules

These are what keep the split clean rather than merely physical:

1. **The AI service is stateless w.r.t. school data.** Spring sends the features it needs in the request body. The AI service does not connect to the operational Postgres. This keeps one writer, one source of truth, and one place where authorization lives.
2. **Every response is advisory and attributed** — a prediction carries `model_version`, `confidence`, and `generated_at`. Spring persists AI output only into clearly-marked columns/tables (`ai_generated = true`). A predicted grade must never be indistinguishable from a real one.
3. **Every call is fail-soft.** Spring calls through `module-ai-client` with a timeout, a retry budget, and a circuit breaker (Resilience4j). AI down ⇒ AI features degrade; attendance, enrolment, and exams keep working.
4. **Async for anything slow.** Batch predictions and report generation go through a job: Spring writes a `job` row, calls the AI service, and the frontend polls or receives an update. Do not hold an HTTP request open for a model run.
5. **The assistant reads through Spring, not around it.** `/v1/assistant/ask` receives the asking user's identity; any data it needs is fetched by calling **back into Spring's REST API with that user's own permissions**. This is the difference between a chatbot and a data-leak: "How many days was John absent?" must be answerable only by John's parent, John, and his teachers — and that check already exists in §7.
6. **Human review before consequence.** Dropout-risk and performance predictions about children carry real weight. They surface to staff as *indicators with reasons*, never as automated decisions, and never directly to parents without staff sign-off. Log who viewed a risk score. Record the features behind each prediction so a flag can be explained and contested — the ability to say *why* is not optional for this kind of output.

### Face recognition — treat as its own project

Biometric attendance is not a feature increment; it is a category change. It involves biometric data belonging to minors. Before any code: confirm the legal basis under the applicable data-protection regime, define consent capture and withdrawal, set a retention period, decide whether templates (not images) are stored, and plan encryption at rest. Assume it needs the school's and guardians' explicit informed consent, and design a non-biometric fallback for anyone who declines — a student who opts out must still be able to be marked present without friction or stigma. Schedule this **last** (phase 5) and give it its own review. If that review does not happen, ship the rest without it; the platform is fully useful without face recognition.

---

## 9. Designing for the gateway and events without building them

| Future component | Cheap thing to do now | Trigger to actually build it |
|---|---|---|
| API Gateway | consistent `/api/v1` prefix, no gateway-specific coupling, CORS in one config class | a second consumer (mobile/partner) appears |
| Kafka | publish through `EventPublisher` interface; in-process implementation today | cross-service async or replay/audit needs |
| Kubernetes | containerise, stateless app, env-var config, `/actuator/health` probes | more than one node's worth of load |
| Separate services | module boundaries + ArchUnit + ID-only cross-module refs | a module needs independent scaling or a separate team owns it |

Each row: pay a few hours now, save weeks later — and pay nothing if the trigger never fires.

---

## 10. Multi-tenancy (decide before `V1__init.sql`)

"Multi-school SaaS" and "multi-campus" are on the roadmap, and this is the one decision that is brutally expensive to retrofit.

Three options, in increasing cost and isolation:

1. **Discriminator column** — `tenant_id` on every tenant-owned table, enforced by a Hibernate filter driven by a `TenantContext` populated from a JWT claim. Cheapest, one schema, one migration run. Risk: a single missed filter is a cross-school data leak.
2. **Schema-per-tenant** — strong isolation, Flyway runs per schema, connection routing by tenant. Moderate cost, good fit for a few dozen schools.
3. **Database-per-tenant** — strongest isolation, highest operational cost. Appropriate only if a government or ministry contract demands it.

**Recommendation: option 1 now, structured so option 2 is reachable.** Concretely, in phase 2: add `tenant_id` to every tenant-owned table in `V1__init.sql`, seed a single default tenant, and route all queries through the filter from day one — even while there is exactly one school. Adding a column to an empty database costs minutes; adding it to a populated production database with live queries costs a migration project.

If multi-school is genuinely not a goal, say so explicitly and skip this — but decide deliberately, not by default.

---

## 11. Migration phases

**Phase 1 — Foundation**
1. Maven multi-module skeleton + `common-library` + ArchUnit boundary tests.
2. Spring Initializr deps: Web, Data JPA, Validation, PostgreSQL, Flyway, OAuth2 Resource Server, Actuator, Resilience4j.
3. `application.yml` wired to existing Postgres + Keycloak env vars.

**Phase 2 — Domain**
4. Port 14 entities + 2 enums into their owning modules; cross-module refs as IDs.
5. `V1__init.sql` from Prisma migrations, **including `tenant_id`** (§10); `ddl-auto=validate`.
6. Repositories, DTOs, MapStruct mappers, Bean Validation mirroring the Zod schemas.

**Phase 3 — Core API**
7. Vertical slice: **Subject** end-to-end (repo → DTO → service → controller → tests) as the reference pattern.
8. Replicate across Class, Teacher, Student, Exam. Port the capacity check and the Keycloak rollback.
9. `KeycloakService` + JWT resource-server config + role converter.
10. `SecurityConfig`, `@PreAuthorize`, **row-level access filtering** (§7), `GlobalExceptionHandler` with Problem Details.
11. Seed: translate `prisma/seed.ts` → `V2__seed.sql` or a dev-profile `CommandLineRunner`.
12. Remaining resources: Lesson, Assignment, Result, Attendance, Event, Announcement, Grade, Parent.

**Phase 4 — Frontend rewire** *(separate effort)*
13. Replace `actions.ts` + inline Prisma reads with `fetch` to `/api/v1/**`, attaching the Keycloak access token; drop `@prisma/client` from the Next app.
14. Cut over resource by resource — both stacks can run side by side against the same database during the transition, which makes this reversible.

**Phase 5 — AI service**
15. FastAPI skeleton, JWT validation against Keycloak, `/health`, `/v1/models`.
16. `module-ai-client` in Spring: typed client, timeouts, Resilience4j circuit breaker, fail-soft defaults.
17. First model: **performance prediction** — the narrowest scope, real training data already in `Result`/`Attendance`, and a clear success metric. Ship one model well before adding a second.
18. Report-comment generation, then the assistant (§8 rule 5 is mandatory here).
19. Face recognition **only after** its own legal/consent review (§8).

**Phase 6 — Operations**
20. `docker-compose.yml`: `school-api` + `ai-service` + postgres (healthcheck) + keycloak.
21. Actuator + Micrometer → Prometheus/Grafana; structured JSON logs with correlation IDs propagated to the AI service.
22. Audit logging for every mutation on student data (who, what, when, before/after).
23. Backup and restore drill — a restore that has never been tested is not a backup.

Phases 1–4 deliver a complete, production-shaped system. Everything after is additive: **if AI never ships, the platform still works.** That property is the main reason to keep AI out of process.

---

## 12. Testing strategy

Enterprise claims need enterprise evidence:

- **Unit** — services with mocked repositories; the capacity check and the Keycloak-rollback path are the highest-value targets.
- **Integration** — **Testcontainers** for Postgres and Keycloak. Real database, real JWTs, real Flyway run. This is what makes the migration verifiable rather than hopeful.
- **Architecture** — ArchUnit: no cross-module `internal` imports, no controller→repository shortcuts, no JPA relationships across boundaries.
- **Security** — an explicit test per role per endpoint, plus negative tests: parent A must receive 403 for student B. Row-level access is exactly the kind of rule that silently regresses.
- **Contract** — pin the Spring↔AI JSON contract in tests on both sides, so the Python service cannot break Java at runtime.
- **Migration parity** — for each ported resource, assert the new endpoint returns the same data as the current Prisma query for a seeded dataset. This is what turns "it compiles" into "it's correct".

---

## 13. Risks and open decisions

| Risk | Mitigation |
|---|---|
| Keycloak/Postgres have no distributed transaction | Compensating delete + `provisioning_audit` logging (§5.2) |
| Multi-tenancy retrofitted late | Decide in phase 2, before `V1__init.sql` (§10) |
| Row-level access left to controllers | Centralised filter + negative tests (§7, §12) |
| Modular monolith degrades into a big ball of mud | ArchUnit in CI, not documentation (§2) |
| AI outage takes down attendance | Circuit breaker + fail-soft (§8.3) |
| AI predictions treated as fact about children | Attribution, explainability, human review (§8.6) |
| Biometric data of minors | Separate legal/consent review; opt-out path; ship last (§8) |
| Big-bang frontend cutover | Resource-by-resource, both stacks live (§11.14) |
| M:N DTO↔entity mapping (`teachers`/`subjects` as ID arrays) | Explicit resolution to managed entities before save |
| Wrong seed source | `prisma/seed.ts` is the real seed; `src/lib/data.ts` is mock UI data — port only the former |

**Open questions to confirm:**
1. Is multi-school SaaS a real goal? (Determines §10 — needed before phase 2.)
2. Re-enable teacher-scoped exam authorization? (Recommend yes, inside §7's row-level work.)
3. Which government integrations are actually committed, and on what timeline? Their auth and data-format requirements can constrain the API contract, so name them before phase 3 freezes it.
4. Expected scale — schools, students, concurrent users at launch and in year two? This is what decides whether §9's triggers are 18 months away or 5 years.

---

## 14. First deliverables (when you say go)

1. Maven multi-module skeleton + `common-library` + ArchUnit boundary tests
2. `app/` module: `SchoolPlatformApplication`, `application.yml`, Docker Compose wiring
3. All 14 entities + 2 enums in their owning modules + `V1__init.sql` (with the §10 tenancy decision applied)
4. **Subject slice end-to-end** as the reference pattern, with Testcontainers integration tests
5. `SecurityConfig` + `JwtAuthenticationConverter` + `KeycloakService`

Then replicate the Subject pattern across the remaining resources. The AI service starts only after phase 4 — it is designed for from day one and built when the core is proven.
