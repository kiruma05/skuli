-- One-time copy of the cut-over resources' data from the legacy Prisma tables (PascalCase) into
-- the backend's tables (snake_case + tenant_id = 'default'), so the backend serves your existing
-- Grade/Subject/Class rows. Run against the shared database AFTER the backend's Flyway migrations
-- have created its tables:
--
--   docker exec -i skuli_postgres psql -U kiruma -d mydb < scripts/migrate-prisma-to-backend.sql
--
-- Idempotent (ON CONFLICT DO NOTHING). Class supervisors are intentionally dropped: they reference
-- Teacher, which is not cut over yet, so its backend table is empty.

INSERT INTO grade (id, tenant_id, level)
SELECT id, 'default', level FROM "Grade"
ON CONFLICT (id) DO NOTHING;

INSERT INTO subject (id, tenant_id, name)
SELECT id, 'default', name FROM "Subject"
ON CONFLICT (id) DO NOTHING;

INSERT INTO classes (id, tenant_id, name, capacity, supervisor_id, grade_id)
SELECT id, 'default', name, capacity, NULL, "gradeId" FROM "Class"
ON CONFLICT (id) DO NOTHING;

-- Advance the SERIAL sequences past the copied ids (handles both empty and populated tables).
SELECT setval('grade_id_seq',   COALESCE((SELECT MAX(id) FROM grade), 1),   (SELECT MAX(id) FROM grade) IS NOT NULL);
SELECT setval('subject_id_seq', COALESCE((SELECT MAX(id) FROM subject), 1), (SELECT MAX(id) FROM subject) IS NOT NULL);
SELECT setval('classes_id_seq', COALESCE((SELECT MAX(id) FROM classes), 1), (SELECT MAX(id) FROM classes) IS NOT NULL);

\echo 'Migrated counts (backend tables):'
SELECT 'grade' AS table, count(*) FROM grade
UNION ALL SELECT 'subject', count(*) FROM subject
UNION ALL SELECT 'classes', count(*) FROM classes;
