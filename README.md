<div align="center">

# Skuli — School Management Platform

**A multi-tenant school management system: a Next.js dashboard backed by a Spring Boot modular monolith, with AI isolated in its own service.**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Next.js](https://img.shields.io/badge/Next.js-14.2-000000?logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Keycloak](https://img.shields.io/badge/Keycloak-OIDC-4D4D4D?logo=keycloak&logoColor=white)](https://www.keycloak.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)

</div>

---

## Overview

Skuli is a role-based school management platform (admin, teacher, student, parent) covering the day-to-day records of a school — students, teachers, classes, subjects, lessons, exams, assignments, results, attendance, events, and announcements.

The project is mid-migration: a working **Next.js** application (originally server actions + Prisma) is having its backend re-platformed onto a **Spring Boot modular monolith**, while Next.js remains the frontend. AI features are deliberately kept out of the JVM, in a separate Python/FastAPI service.

> Full design rationale and the phased plan live in [`SPRINGBOOT_MIGRATION_PLAN.md`](./SPRINGBOOT_MIGRATION_PLAN.md).

## Architecture

```
┌──────────────────────┐        ┌─────────────────────────────────────┐
│   Next.js frontend    │  OIDC  │            Keycloak                  │
│   (dashboard, :3000)  │◄──────►│   (auth, realm "skuli", :8080)       │
└──────────┬───────────┘        └─────────────────────────────────────┘
           │ REST + JWT
           ▼
┌──────────────────────┐        ┌─────────────────────────────────────┐
│  Spring Boot backend  │◄──────►│           PostgreSQL 15              │
│  modular monolith      │  JPA   │            (:5433)                   │
│  (:8081)               │        └─────────────────────────────────────┘
└──────────┬───────────┘
           │ typed client (Resilience4j)
           ▼
┌──────────────────────┐
│   AI service (Python)  │   ← planned, isolated from the JVM
│   FastAPI              │
└──────────────────────┘
```

Three guiding decisions:

1. **Spring Boot is the core** — transactional CRUD, role-based access, auditability, and future government/payment/SMS integrations.
2. **AI lives outside the JVM**, in Python — the AI service owns models and inference; Spring owns records and rules.
3. **Modular monolith, not microservices** — Maven modules with **enforced boundaries** (ArchUnit), deployed as one JAR.

## Tech stack

| Layer        | Technology                                                                 |
|--------------|----------------------------------------------------------------------------|
| Frontend     | Next.js 14, React 18, NextAuth v5, TypeScript, Tailwind CSS, Zod, React Hook Form |
| Backend      | Spring Boot 3.3.5, Java 21, Spring Web / Data JPA / Security / OAuth2 Resource Server, Actuator, Flyway |
| Data         | PostgreSQL 15 (Prisma today; Flyway-managed JPA on the backend)             |
| Auth         | Keycloak (OpenID Connect), multi-tenant via `tenant_id` + tenant context filter |
| AI (planned) | Python / FastAPI, called through a Resilience4j-guarded client              |
| Tooling      | Docker Compose, Maven multi-module, ArchUnit                               |

## Project structure

```
skul/
├── docker-compose.yml            # Base: PostgreSQL + shared network (project "skuli")
├── docker-compose.backend.yml    # Backend overlay (Spring Boot)
├── docker-compose.frontend.yml   # Frontend overlay (Next.js)
├── SPRINGBOOT_MIGRATION_PLAN.md  # Architecture & phased migration plan
│
├── full-stack-school-main/       # Next.js frontend
│   ├── src/app/                  #   role dashboards + resource list pages
│   └── prisma/                   #   schema (14 models, 2 enums) + migrations + seed
│
└── school-platform/              # Spring Boot backend (Maven multi-module)
    ├── app/                      #   application entrypoint + security config
    ├── common-library/           #   errors, domain events, security, tenant context
    └── module-{auth,student,staff,academics,finance,communication,reporting,ai-client}/
```

## Getting started

### Prerequisites

- Docker + Docker Compose
- A running **Keycloak** container with a realm named `skuli` (Keycloak runs as its own container, outside this compose project)

### 1. Host entry for OIDC

The browser and the containers must reach Keycloak at the *same* hostname. Add this once:

```bash
echo "127.0.0.1 host.docker.internal" | sudo tee -a /etc/hosts
```

### 2. Frontend environment

```bash
cp full-stack-school-main/.env.example full-stack-school-main/.env
# then fill in AUTH_KEYCLOAK_ID / _SECRET and Cloudinary values
```

### 3. Bring up the stack

```bash
docker compose up -d --build
```

That starts PostgreSQL, the backend, and the frontend under project `skuli`.

| Service    | URL                                             |
|------------|-------------------------------------------------|
| Frontend   | http://localhost:3000                           |
| Backend    | http://localhost:8081 (health `/actuator/health`) |
| PostgreSQL | `localhost:5433` (`postgres:5432` inside the network) |
| Keycloak   | http://host.docker.internal:8080 (external)     |

## Development — run services independently

Compose is split into a shared **base** plus one **overlay per app**, all pinned to the same project name (`skuli`) so they share one network and one database volume:

```bash
# Backend only (+ its Postgres dependency)
docker compose -f docker-compose.yml -f docker-compose.backend.yml up -d --build

# Frontend only
docker compose -f docker-compose.yml -f docker-compose.frontend.yml up -d --build
```

Each app can be rebuilt or restarted without disturbing the other.
> ⚠️ Do **not** pass `--remove-orphans` on a single-app command — it would remove the other app from the project.

## Roadmap

| Phase | Scope                                                        | Status |
|-------|-------------------------------------------------------------|--------|
| 1     | Foundation — multi-module skeleton, common-library, security, config | ✅ Complete |
| 2     | Domain — port 14 entities + 2 enums, Flyway `V1__init.sql`, repositories/DTOs/validation | 🔜 Next |
| 3     | Core API — vertical slices, Keycloak integration, row-level access | ⬜ Planned |
| 4     | Frontend rewire — Next.js calls `/api/v1/**` instead of Prisma | ⬜ Planned |
| 5     | AI service — FastAPI, performance prediction, report generation | ⬜ Planned |
| 6     | Operations — observability, audit logging, backup/restore drills | ⬜ Planned |

See [`SPRINGBOOT_MIGRATION_PLAN.md`](./SPRINGBOOT_MIGRATION_PLAN.md) for the detailed breakdown.

## License

No license has been declared yet. All rights reserved by the author until one is added.
