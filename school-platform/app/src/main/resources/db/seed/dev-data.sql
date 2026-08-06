-- =====================================================================================
-- dev-data.sql — representative development seed for the single default tenant.
--
-- Loaded only by DevDataSeeder under the "dev" profile (never in production), and only when the
-- database is empty. Ids are explicit so cross-references are readable; the SERIAL sequences are
-- advanced at the end so the API can keep inserting without primary-key clashes.
--
-- NOTE: the people below exist in the database only — they are NOT provisioned in Keycloak, so
-- they cannot authenticate. They are fixtures for exercising reads/writes against seeded data.
-- =====================================================================================

INSERT INTO admin (id, tenant_id, username) VALUES
    ('admin1', 'default', 'admin1');

INSERT INTO grade (id, tenant_id, level) VALUES
    (1, 'default', 1),
    (2, 'default', 2),
    (3, 'default', 3);

INSERT INTO subject (id, tenant_id, name) VALUES
    (1, 'default', 'Mathematics'),
    (2, 'default', 'English'),
    (3, 'default', 'Science');

INSERT INTO teacher
    (id, tenant_id, username, name, surname, email, phone, address, img, blood_type, sex, birthday)
VALUES
    ('teacher1', 'default', 'teacher1', 'Tina', 'Teacher', 'tina@example.com', '555-0101',
     '1 Faculty Row', NULL, 'O+', 'FEMALE', TIMESTAMP '1985-05-05 00:00:00');

INSERT INTO subject_teachers (teacher_id, subject_id) VALUES
    ('teacher1', 1);

INSERT INTO classes (id, tenant_id, name, capacity, supervisor_id, grade_id) VALUES
    (1, 'default', '1A', 20, 'teacher1', 1),
    (2, 'default', '2A', 20, NULL, 2);

INSERT INTO parent
    (id, tenant_id, username, name, surname, email, phone, address)
VALUES
    ('parent1', 'default', 'parent1', 'Pam', 'Parent', 'pam@example.com', '555-1000', '1 Home St');

INSERT INTO student
    (id, tenant_id, username, name, surname, email, phone, address, img, blood_type, sex,
     birthday, parent_id, class_id, grade_id)
VALUES
    ('student1', 'default', 'student1', 'Sam', 'Student', NULL, NULL, '1 Home St', NULL, 'A+',
     'MALE', TIMESTAMP '2012-03-03 00:00:00', 'parent1', 1, 1),
    ('student2', 'default', 'student2', 'Sara', 'Student', NULL, NULL, '1 Home St', NULL, 'B+',
     'FEMALE', TIMESTAMP '2012-07-07 00:00:00', 'parent1', 1, 1);

INSERT INTO lesson
    (id, tenant_id, name, day, start_time, end_time, subject_id, class_id, teacher_id)
VALUES
    (1, 'default', 'Algebra', 'MONDAY', TIMESTAMP '2026-01-05 09:00:00',
     TIMESTAMP '2026-01-05 10:00:00', 1, 1, 'teacher1');

INSERT INTO exam (id, tenant_id, title, start_time, end_time, lesson_id) VALUES
    (1, 'default', 'Algebra Midterm', TIMESTAMP '2026-02-01 09:00:00',
     TIMESTAMP '2026-02-01 10:00:00', 1);

INSERT INTO assignment (id, tenant_id, title, start_date, due_date, lesson_id) VALUES
    (1, 'default', 'Algebra Homework 1', TIMESTAMP '2026-01-06 09:00:00',
     TIMESTAMP '2026-01-13 09:00:00', 1);

INSERT INTO result (id, tenant_id, score, exam_id, assignment_id, student_id) VALUES
    (1, 'default', 88, 1, NULL, 'student1'),
    (2, 'default', 72, NULL, 1, 'student2');

INSERT INTO attendance (id, tenant_id, date, present, student_id, lesson_id) VALUES
    (1, 'default', TIMESTAMP '2026-01-05 09:00:00', TRUE, 'student1', 1),
    (2, 'default', TIMESTAMP '2026-01-05 09:00:00', FALSE, 'student2', 1);

INSERT INTO event (id, tenant_id, title, description, start_time, end_time, class_id) VALUES
    (1, 'default', 'Parents Evening', 'Meet the teachers', TIMESTAMP '2026-03-01 17:00:00',
     TIMESTAMP '2026-03-01 19:00:00', 1);

INSERT INTO announcement (id, tenant_id, title, description, date, class_id) VALUES
    (1, 'default', 'Term Starts', 'Spring term begins Monday', TIMESTAMP '2026-01-05 08:00:00', NULL);

-- Advance the SERIAL sequences past the explicit ids seeded above.
SELECT setval('grade_id_seq', (SELECT MAX(id) FROM grade));
SELECT setval('subject_id_seq', (SELECT MAX(id) FROM subject));
SELECT setval('classes_id_seq', (SELECT MAX(id) FROM classes));
SELECT setval('lesson_id_seq', (SELECT MAX(id) FROM lesson));
SELECT setval('exam_id_seq', (SELECT MAX(id) FROM exam));
SELECT setval('assignment_id_seq', (SELECT MAX(id) FROM assignment));
SELECT setval('result_id_seq', (SELECT MAX(id) FROM result));
SELECT setval('attendance_id_seq', (SELECT MAX(id) FROM attendance));
SELECT setval('event_id_seq', (SELECT MAX(id) FROM event));
SELECT setval('announcement_id_seq', (SELECT MAX(id) FROM announcement));
