-- One-time migration of ALL data from the legacy Prisma tables (PascalCase) into the backend's
-- tables (snake_case + tenant_id = 'default'). Part of "backend as source of truth" (Phase 4): once
-- this has run and every frontend resource is cut over, the Prisma tables can be dropped.
--
-- Run against the shared database AFTER the backend's Flyway migrations have created its tables:
--   docker exec -i skuli_postgres psql -U kiruma -d mydb < scripts/migrate-prisma-to-backend.sql
--
-- Idempotent (ON CONFLICT DO NOTHING). Copies in FK dependency order. Prisma enums (UserSex, Day)
-- are cast to text to match the backend's VARCHAR + CHECK columns.

-- 1. Reference data (no dependencies)
INSERT INTO grade (id, tenant_id, level)
SELECT id, 'default', level FROM "Grade" ON CONFLICT (id) DO NOTHING;

INSERT INTO subject (id, tenant_id, name)
SELECT id, 'default', name FROM "Subject" ON CONFLICT (id) DO NOTHING;

-- 2. People
INSERT INTO admin (id, tenant_id, username)
SELECT id, 'default', username FROM "Admin" ON CONFLICT (id) DO NOTHING;

INSERT INTO teacher (id, tenant_id, username, name, surname, email, phone, address, img,
                     blood_type, sex, birthday, created_at)
SELECT id, 'default', username, name, surname, email, phone, address, img,
       "bloodType", sex::text, birthday, "createdAt"
FROM "Teacher" ON CONFLICT (id) DO NOTHING;

INSERT INTO parent (id, tenant_id, username, name, surname, email, phone, address, created_at)
SELECT id, 'default', username, name, surname, email, phone, address, "createdAt"
FROM "Parent" ON CONFLICT (id) DO NOTHING;

-- 3. Classes (supervisor -> teacher, grade -> grade)
INSERT INTO classes (id, tenant_id, name, capacity, supervisor_id, grade_id)
SELECT id, 'default', name, capacity, "supervisorId", "gradeId" FROM "Class"
ON CONFLICT (id) DO NOTHING;
-- Backfill supervisors for classes copied by the earlier partial migration (which nulled them).
UPDATE classes c SET supervisor_id = pc."supervisorId"
FROM "Class" pc WHERE c.id = pc.id AND pc."supervisorId" IS NOT NULL AND c.supervisor_id IS NULL;

-- 4. Teacher<->Subject assignment
INSERT INTO subject_teachers (teacher_id, subject_id)
SELECT "B", "A" FROM "_SubjectToTeacher" ON CONFLICT DO NOTHING;

-- 5. Students (parent/class/grade)
INSERT INTO student (id, tenant_id, username, name, surname, email, phone, address, img,
                     blood_type, sex, birthday, created_at, parent_id, class_id, grade_id)
SELECT id, 'default', username, name, surname, email, phone, address, img,
       "bloodType", sex::text, birthday, "createdAt", "parentId", "classId", "gradeId"
FROM "Student" ON CONFLICT (id) DO NOTHING;

-- 6. Academics referencing the above
INSERT INTO lesson (id, tenant_id, name, day, start_time, end_time, subject_id, class_id, teacher_id)
SELECT id, 'default', name, day::text, "startTime", "endTime", "subjectId", "classId", "teacherId"
FROM "Lesson" ON CONFLICT (id) DO NOTHING;

INSERT INTO exam (id, tenant_id, title, start_time, end_time, lesson_id)
SELECT id, 'default', title, "startTime", "endTime", "lessonId" FROM "Exam"
ON CONFLICT (id) DO NOTHING;

INSERT INTO assignment (id, tenant_id, title, start_date, due_date, lesson_id)
SELECT id, 'default', title, "startDate", "dueDate", "lessonId" FROM "Assignment"
ON CONFLICT (id) DO NOTHING;

INSERT INTO result (id, tenant_id, score, exam_id, assignment_id, student_id)
SELECT id, 'default', score, "examId", "assignmentId", "studentId" FROM "Result"
ON CONFLICT (id) DO NOTHING;

INSERT INTO attendance (id, tenant_id, date, present, student_id, lesson_id)
SELECT id, 'default', date, present, "studentId", "lessonId" FROM "Attendance"
ON CONFLICT (id) DO NOTHING;

-- 7. Communication (class is optional)
INSERT INTO event (id, tenant_id, title, description, start_time, end_time, class_id)
SELECT id, 'default', title, description, "startTime", "endTime", "classId" FROM "Event"
ON CONFLICT (id) DO NOTHING;

INSERT INTO announcement (id, tenant_id, title, description, date, class_id)
SELECT id, 'default', title, description, date, "classId" FROM "Announcement"
ON CONFLICT (id) DO NOTHING;

-- 8. Advance SERIAL sequences past the copied ids (handles empty and populated tables).
SELECT setval('grade_id_seq',        COALESCE((SELECT MAX(id) FROM grade), 1),        (SELECT MAX(id) FROM grade) IS NOT NULL);
SELECT setval('subject_id_seq',      COALESCE((SELECT MAX(id) FROM subject), 1),      (SELECT MAX(id) FROM subject) IS NOT NULL);
SELECT setval('classes_id_seq',      COALESCE((SELECT MAX(id) FROM classes), 1),      (SELECT MAX(id) FROM classes) IS NOT NULL);
SELECT setval('lesson_id_seq',       COALESCE((SELECT MAX(id) FROM lesson), 1),       (SELECT MAX(id) FROM lesson) IS NOT NULL);
SELECT setval('exam_id_seq',         COALESCE((SELECT MAX(id) FROM exam), 1),         (SELECT MAX(id) FROM exam) IS NOT NULL);
SELECT setval('assignment_id_seq',   COALESCE((SELECT MAX(id) FROM assignment), 1),   (SELECT MAX(id) FROM assignment) IS NOT NULL);
SELECT setval('result_id_seq',       COALESCE((SELECT MAX(id) FROM result), 1),       (SELECT MAX(id) FROM result) IS NOT NULL);
SELECT setval('attendance_id_seq',   COALESCE((SELECT MAX(id) FROM attendance), 1),   (SELECT MAX(id) FROM attendance) IS NOT NULL);
SELECT setval('event_id_seq',        COALESCE((SELECT MAX(id) FROM event), 1),        (SELECT MAX(id) FROM event) IS NOT NULL);
SELECT setval('announcement_id_seq', COALESCE((SELECT MAX(id) FROM announcement), 1), (SELECT MAX(id) FROM announcement) IS NOT NULL);
