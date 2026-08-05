-- =====================================================================================
-- V1__init.sql — initial schema for the Spring Boot backend.
--
-- Translated from the legacy Prisma migrations (full-stack-school-main/prisma/migrations),
-- with three deliberate changes for the modular monolith:
--
--   1. Multi-tenancy: every tenant-owned table carries a NOT NULL `tenant_id` referencing
--      `tenant(id)`. A single default tenant is seeded so the column is usable from day one.
--      Tenant-scoped natural keys (grade level, class/subject name) are unique PER TENANT;
--      identity keys (username/email/phone) stay globally unique because id == username and
--      the Keycloak realm is shared across schools.
--   2. Naming: snake_case, lowercase, unquoted identifiers to match Hibernate's default
--      physical naming strategy (ddl-auto=validate). The Prisma `Class` model becomes table
--      `classes` because `class` is a SQL reserved word.
--   3. Cross-module foreign keys are kept at the DB level for integrity even though the JPA
--      entities hold the referenced id as a scalar (no object relationship across a module
--      boundary). Boundary integrity in the database, boundary discipline in the code.
-- =====================================================================================

-- ---------------------------------------------------------------------------------------
-- Enums (ported as native Postgres enums; entities map them with @Enumerated(STRING))
-- ---------------------------------------------------------------------------------------
CREATE TYPE user_sex AS ENUM ('MALE', 'FEMALE');
CREATE TYPE day AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY');

-- Hibernate maps a Java enum to VARCHAR by default, not to a Postgres enum type. To keep
-- ddl-auto=validate happy we store these as VARCHAR with a CHECK constraint rather than the
-- native enum types above. Drop the native types again — they are documented here only to
-- show the source domain.
DROP TYPE user_sex;
DROP TYPE day;

-- ---------------------------------------------------------------------------------------
-- Tenant registry
-- ---------------------------------------------------------------------------------------
CREATE TABLE tenant (
    id         VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tenant_pkey PRIMARY KEY (id)
);

INSERT INTO tenant (id, name) VALUES ('default', 'Default School');

-- ---------------------------------------------------------------------------------------
-- Identity-owned tables (module-staff / module-student)
-- ---------------------------------------------------------------------------------------
CREATE TABLE admin (
    id        VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    username  VARCHAR(255) NOT NULL,
    CONSTRAINT admin_pkey PRIMARY KEY (id)
);

CREATE TABLE parent (
    id         VARCHAR(255) NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    surname    VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(255) NOT NULL,
    address    VARCHAR(500) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT parent_pkey PRIMARY KEY (id)
);

CREATE TABLE teacher (
    id         VARCHAR(255) NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    surname    VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(255),
    address    VARCHAR(500) NOT NULL,
    img        VARCHAR(500),
    blood_type VARCHAR(255) NOT NULL,
    sex        VARCHAR(255) NOT NULL,
    birthday   TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT teacher_pkey PRIMARY KEY (id),
    CONSTRAINT teacher_sex_check CHECK (sex IN ('MALE', 'FEMALE'))
);

-- ---------------------------------------------------------------------------------------
-- Academic structure (module-academics)
-- ---------------------------------------------------------------------------------------
CREATE TABLE grade (
    id        SERIAL       NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    level     INTEGER      NOT NULL,
    CONSTRAINT grade_pkey PRIMARY KEY (id)
);

CREATE TABLE classes (
    id            SERIAL       NOT NULL,
    tenant_id     VARCHAR(255) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    capacity      INTEGER      NOT NULL,
    supervisor_id VARCHAR(255),
    grade_id      INTEGER      NOT NULL,
    CONSTRAINT classes_pkey PRIMARY KEY (id)
);

CREATE TABLE subject (
    id        SERIAL       NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    name      VARCHAR(255) NOT NULL,
    CONSTRAINT subject_pkey PRIMARY KEY (id)
);

CREATE TABLE student (
    id         VARCHAR(255) NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    surname    VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(255),
    address    VARCHAR(500) NOT NULL,
    img        VARCHAR(500),
    blood_type VARCHAR(255) NOT NULL,
    sex        VARCHAR(255) NOT NULL,
    birthday   TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    parent_id  VARCHAR(255) NOT NULL,
    class_id   INTEGER      NOT NULL,
    grade_id   INTEGER      NOT NULL,
    CONSTRAINT student_pkey PRIMARY KEY (id),
    CONSTRAINT student_sex_check CHECK (sex IN ('MALE', 'FEMALE'))
);

CREATE TABLE lesson (
    id         SERIAL       NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    day        VARCHAR(255) NOT NULL,
    start_time TIMESTAMP(6) NOT NULL,
    end_time   TIMESTAMP(6) NOT NULL,
    subject_id INTEGER      NOT NULL,
    class_id   INTEGER      NOT NULL,
    teacher_id VARCHAR(255) NOT NULL,
    CONSTRAINT lesson_pkey PRIMARY KEY (id),
    CONSTRAINT lesson_day_check CHECK (day IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'))
);

CREATE TABLE exam (
    id         SERIAL       NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    start_time TIMESTAMP(6) NOT NULL,
    end_time   TIMESTAMP(6) NOT NULL,
    lesson_id  INTEGER      NOT NULL,
    CONSTRAINT exam_pkey PRIMARY KEY (id)
);

CREATE TABLE assignment (
    id         SERIAL       NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    start_date TIMESTAMP(6) NOT NULL,
    due_date   TIMESTAMP(6) NOT NULL,
    lesson_id  INTEGER      NOT NULL,
    CONSTRAINT assignment_pkey PRIMARY KEY (id)
);

CREATE TABLE result (
    id            SERIAL       NOT NULL,
    tenant_id     VARCHAR(255) NOT NULL,
    score         INTEGER      NOT NULL,
    exam_id       INTEGER,
    assignment_id INTEGER,
    student_id    VARCHAR(255) NOT NULL,
    CONSTRAINT result_pkey PRIMARY KEY (id)
);

CREATE TABLE attendance (
    id         SERIAL       NOT NULL,
    tenant_id  VARCHAR(255) NOT NULL,
    date       TIMESTAMP(6) NOT NULL,
    present    BOOLEAN      NOT NULL,
    student_id VARCHAR(255) NOT NULL,
    lesson_id  INTEGER      NOT NULL,
    CONSTRAINT attendance_pkey PRIMARY KEY (id)
);

-- Teacher <-> Subject assignment (Prisma implicit _SubjectToTeacher). Owned by module-staff.
-- Tenancy is implied by its parent rows, so no tenant_id column here.
CREATE TABLE subject_teachers (
    teacher_id VARCHAR(255) NOT NULL,
    subject_id INTEGER      NOT NULL,
    CONSTRAINT subject_teachers_pkey PRIMARY KEY (teacher_id, subject_id)
);

-- ---------------------------------------------------------------------------------------
-- Communication (module-communication)
-- ---------------------------------------------------------------------------------------
CREATE TABLE event (
    id          SERIAL       NOT NULL,
    tenant_id   VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    start_time  TIMESTAMP(6) NOT NULL,
    end_time    TIMESTAMP(6) NOT NULL,
    class_id    INTEGER,
    CONSTRAINT event_pkey PRIMARY KEY (id)
);

CREATE TABLE announcement (
    id          SERIAL       NOT NULL,
    tenant_id   VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    date        TIMESTAMP(6) NOT NULL,
    class_id    INTEGER,
    CONSTRAINT announcement_pkey PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------------------------
-- Uniqueness — global identity keys vs per-tenant natural keys
-- ---------------------------------------------------------------------------------------
CREATE UNIQUE INDEX admin_username_key    ON admin (username);
CREATE UNIQUE INDEX parent_username_key   ON parent (username);
CREATE UNIQUE INDEX parent_email_key      ON parent (email);
CREATE UNIQUE INDEX parent_phone_key      ON parent (phone);
CREATE UNIQUE INDEX teacher_username_key  ON teacher (username);
CREATE UNIQUE INDEX teacher_email_key     ON teacher (email);
CREATE UNIQUE INDEX teacher_phone_key     ON teacher (phone);
CREATE UNIQUE INDEX student_username_key  ON student (username);
CREATE UNIQUE INDEX student_email_key     ON student (email);
CREATE UNIQUE INDEX student_phone_key     ON student (phone);

CREATE UNIQUE INDEX grade_level_key   ON grade (tenant_id, level);
CREATE UNIQUE INDEX classes_name_key  ON classes (tenant_id, name);
CREATE UNIQUE INDEX subject_name_key  ON subject (tenant_id, name);

-- ---------------------------------------------------------------------------------------
-- Foreign keys — tenant scoping first, then domain references (kept even across modules)
-- ---------------------------------------------------------------------------------------
ALTER TABLE admin        ADD CONSTRAINT admin_tenant_fkey        FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE parent       ADD CONSTRAINT parent_tenant_fkey       FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE teacher      ADD CONSTRAINT teacher_tenant_fkey      FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE student      ADD CONSTRAINT student_tenant_fkey      FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE grade        ADD CONSTRAINT grade_tenant_fkey        FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE classes      ADD CONSTRAINT classes_tenant_fkey      FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE subject      ADD CONSTRAINT subject_tenant_fkey      FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE lesson       ADD CONSTRAINT lesson_tenant_fkey       FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE exam         ADD CONSTRAINT exam_tenant_fkey         FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE assignment   ADD CONSTRAINT assignment_tenant_fkey   FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE result       ADD CONSTRAINT result_tenant_fkey       FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE attendance   ADD CONSTRAINT attendance_tenant_fkey   FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE event        ADD CONSTRAINT event_tenant_fkey        FOREIGN KEY (tenant_id) REFERENCES tenant (id);
ALTER TABLE announcement ADD CONSTRAINT announcement_tenant_fkey FOREIGN KEY (tenant_id) REFERENCES tenant (id);

ALTER TABLE student ADD CONSTRAINT student_parent_fkey FOREIGN KEY (parent_id) REFERENCES parent (id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE student ADD CONSTRAINT student_class_fkey  FOREIGN KEY (class_id)  REFERENCES classes (id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE student ADD CONSTRAINT student_grade_fkey  FOREIGN KEY (grade_id)  REFERENCES grade (id)   ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE classes ADD CONSTRAINT classes_supervisor_fkey FOREIGN KEY (supervisor_id) REFERENCES teacher (id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE classes ADD CONSTRAINT classes_grade_fkey      FOREIGN KEY (grade_id)      REFERENCES grade (id)   ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE lesson ADD CONSTRAINT lesson_subject_fkey FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE lesson ADD CONSTRAINT lesson_class_fkey   FOREIGN KEY (class_id)   REFERENCES classes (id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE lesson ADD CONSTRAINT lesson_teacher_fkey FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE exam       ADD CONSTRAINT exam_lesson_fkey       FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE assignment ADD CONSTRAINT assignment_lesson_fkey FOREIGN KEY (lesson_id) REFERENCES lesson (id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE result ADD CONSTRAINT result_exam_fkey       FOREIGN KEY (exam_id)       REFERENCES exam (id)       ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE result ADD CONSTRAINT result_assignment_fkey FOREIGN KEY (assignment_id) REFERENCES assignment (id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE result ADD CONSTRAINT result_student_fkey    FOREIGN KEY (student_id)    REFERENCES student (id)    ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE attendance ADD CONSTRAINT attendance_student_fkey FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE attendance ADD CONSTRAINT attendance_lesson_fkey  FOREIGN KEY (lesson_id)  REFERENCES lesson (id)  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE event        ADD CONSTRAINT event_class_fkey        FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE announcement ADD CONSTRAINT announcement_class_fkey FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE subject_teachers ADD CONSTRAINT subject_teachers_teacher_fkey FOREIGN KEY (teacher_id) REFERENCES teacher (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE subject_teachers ADD CONSTRAINT subject_teachers_subject_fkey FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE ON UPDATE CASCADE;
