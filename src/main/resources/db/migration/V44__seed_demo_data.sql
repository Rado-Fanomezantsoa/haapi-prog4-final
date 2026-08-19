-- V44 seed idempotent (ne casse pas si données déjà présentes)

INSERT INTO "promotion" (id, code, entry_calendar_year, expected_graduation_year)
SELECT '11111111-1111-1111-1111-111111111111', 'K', 2024, 2027
WHERE NOT EXISTS (
  SELECT 1 FROM "promotion" WHERE id = '11111111-1111-1111-1111-111111111111'
);

INSERT INTO "app_user" (id, email, password_hash, first_name, last_name, role)
SELECT '22222222-2222-2222-2222-222222222222',
       'admin@hei.school', 'password', 'Ada', 'Admin', 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM "app_user"
  WHERE email = 'admin@hei.school'
     OR id = '22222222-2222-2222-2222-222222222222'
);

INSERT INTO "app_user" (id, email, password_hash, first_name, last_name, role, teacher_ref)
SELECT '33333333-3333-3333-3333-333333333333',
       'teacher@hei.school', 'password', 'Tiana', 'Teacher', 'TEACHER', 'TCH001'
WHERE NOT EXISTS (
  SELECT 1 FROM "app_user"
  WHERE email = 'teacher@hei.school'
     OR teacher_ref = 'TCH001'
     OR id = '33333333-3333-3333-3333-333333333333'
);

INSERT INTO "app_user" (
  id, email, password_hash, first_name, last_name, role,
  student_ref, promotion_id, specialization, enrolled_at
)
SELECT '44444444-4444-4444-4444-444444444444',
       'std24001@hei.school', 'password', 'Jean', 'Rakoto', 'STUDENT',
       'STD24001', '11111111-1111-1111-1111-111111111111', 'EL', '2024-09-01'
WHERE NOT EXISTS (
  SELECT 1 FROM "app_user"
  WHERE email = 'std24001@hei.school'
     OR student_ref = 'STD24001'
     OR id = '44444444-4444-4444-4444-444444444444'
);

INSERT INTO "app_user" (
  id, email, password_hash, first_name, last_name, role,
  student_ref, promotion_id, specialization, enrolled_at
)
SELECT '55555555-5555-5555-5555-555555555555',
       'std24002@hei.school', 'password', 'Marie', 'Rabe', 'STUDENT',
       'STD24002', '11111111-1111-1111-1111-111111111111', 'EL', '2024-09-01'
WHERE NOT EXISTS (
  SELECT 1 FROM "app_user"
  WHERE email = 'std24002@hei.school'
     OR student_ref = 'STD24002'
     OR id = '55555555-5555-5555-5555-555555555555'
);

INSERT INTO "app_user" (
  id, email, password_hash, first_name, last_name, role,
  student_ref, promotion_id, specialization, enrolled_at
)
SELECT '66666666-6666-6666-6666-666666666666',
       'std24003@hei.school', 'password', 'Paul', 'Andria', 'STUDENT',
       'STD24003', '11111111-1111-1111-1111-111111111111', 'TN', '2024-09-01'
WHERE NOT EXISTS (
  SELECT 1 FROM "app_user"
  WHERE email = 'std24003@hei.school'
     OR student_ref = 'STD24003'
     OR id = '66666666-6666-6666-6666-666666666666'
);

INSERT INTO "class_group" (id, promotion_id, ref, academic_level, specialization)
SELECT 'a1111111-1111-1111-1111-111111111111',
       '11111111-1111-1111-1111-111111111111', 'G-EL', 'L3', 'EL'
WHERE NOT EXISTS (
  SELECT 1 FROM "class_group" WHERE id = 'a1111111-1111-1111-1111-111111111111'
);

INSERT INTO "class_group" (id, promotion_id, ref, academic_level, specialization)
SELECT 'a2222222-2222-2222-2222-222222222222',
       '11111111-1111-1111-1111-111111111111', 'G-TN', 'L3', 'TN'
WHERE NOT EXISTS (
  SELECT 1 FROM "class_group" WHERE id = 'a2222222-2222-2222-2222-222222222222'
);

INSERT INTO "student_group_membership" (id, student_id, group_id, start_date, end_date)
SELECT 'b1111111-1111-1111-1111-111111111111',
       '44444444-4444-4444-4444-444444444444',
       'a1111111-1111-1111-1111-111111111111',
       '2024-09-01', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM "student_group_membership"
  WHERE id = 'b1111111-1111-1111-1111-111111111111'
);

INSERT INTO "student_group_membership" (id, student_id, group_id, start_date, end_date)
SELECT 'b2222222-2222-2222-2222-222222222222',
       '55555555-5555-5555-5555-555555555555',
       'a1111111-1111-1111-1111-111111111111',
       '2024-09-01', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM "student_group_membership"
  WHERE id = 'b2222222-2222-2222-2222-222222222222'
);

INSERT INTO "student_group_membership" (id, student_id, group_id, start_date, end_date)
SELECT 'b3333333-3333-3333-3333-333333333333',
       '66666666-6666-6666-6666-666666666666',
       'a2222222-2222-2222-2222-222222222222',
       '2024-09-01', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM "student_group_membership"
  WHERE id = 'b3333333-3333-3333-3333-333333333333'
);

INSERT INTO "course" (id, ref, title, credits, semester_number, specialization)
SELECT 'c1111111-1111-1111-1111-111111111111',
       'SYS1', 'Systemes 1', 6, 1, 'COMMUN'
WHERE NOT EXISTS (SELECT 1 FROM "course" WHERE ref = 'SYS1' OR id = 'c1111111-1111-1111-1111-111111111111');

INSERT INTO "course" (id, ref, title, credits, semester_number, specialization)
SELECT 'c2222222-2222-2222-2222-222222222222',
       'PROG4', 'Programmation 4', 6, 4, 'EL'
WHERE NOT EXISTS (SELECT 1 FROM "course" WHERE ref = 'PROG4' OR id = 'c2222222-2222-2222-2222-222222222222');

INSERT INTO "course" (id, ref, title, credits, semester_number, specialization)
SELECT 'c3333333-3333-3333-3333-333333333333',
       'WEB4', 'Web 4', 5, 4, 'TN'
WHERE NOT EXISTS (SELECT 1 FROM "course" WHERE ref = 'WEB4' OR id = 'c3333333-3333-3333-3333-333333333333');

INSERT INTO "course_offering" (id, course_id, calendar_year)
SELECT 'd1111111-1111-1111-1111-111111111111',
       'c1111111-1111-1111-1111-111111111111', 2025
WHERE NOT EXISTS (SELECT 1 FROM "course_offering" WHERE id = 'd1111111-1111-1111-1111-111111111111');

INSERT INTO "course_offering" (id, course_id, calendar_year)
SELECT 'd2222222-2222-2222-2222-222222222222',
       'c2222222-2222-2222-2222-222222222222', 2025
WHERE NOT EXISTS (SELECT 1 FROM "course_offering" WHERE id = 'd2222222-2222-2222-2222-222222222222');

INSERT INTO "course_offering" (id, course_id, calendar_year)
SELECT 'd3333333-3333-3333-3333-333333333333',
       'c3333333-3333-3333-3333-333333333333', 2025
WHERE NOT EXISTS (SELECT 1 FROM "course_offering" WHERE id = 'd3333333-3333-3333-3333-333333333333');

INSERT INTO "course_offering_teacher" (course_offering_id, teacher_id)
SELECT 'd1111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_teacher"
  WHERE course_offering_id = 'd1111111-1111-1111-1111-111111111111'
    AND teacher_id = '33333333-3333-3333-3333-333333333333'
);

INSERT INTO "course_offering_teacher" (course_offering_id, teacher_id)
SELECT 'd2222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_teacher"
  WHERE course_offering_id = 'd2222222-2222-2222-2222-222222222222'
    AND teacher_id = '33333333-3333-3333-3333-333333333333'
);

INSERT INTO "course_offering_teacher" (course_offering_id, teacher_id)
SELECT 'd3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_teacher"
  WHERE course_offering_id = 'd3333333-3333-3333-3333-333333333333'
    AND teacher_id = '33333333-3333-3333-3333-333333333333'
);

INSERT INTO "course_offering_group" (course_offering_id, group_id)
SELECT 'd1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_group"
  WHERE course_offering_id = 'd1111111-1111-1111-1111-111111111111'
    AND group_id = 'a1111111-1111-1111-1111-111111111111'
);

INSERT INTO "course_offering_group" (course_offering_id, group_id)
SELECT 'd1111111-1111-1111-1111-111111111111', 'a2222222-2222-2222-2222-222222222222'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_group"
  WHERE course_offering_id = 'd1111111-1111-1111-1111-111111111111'
    AND group_id = 'a2222222-2222-2222-2222-222222222222'
);

INSERT INTO "course_offering_group" (course_offering_id, group_id)
SELECT 'd2222222-2222-2222-2222-222222222222', 'a1111111-1111-1111-1111-111111111111'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_group"
  WHERE course_offering_id = 'd2222222-2222-2222-2222-222222222222'
    AND group_id = 'a1111111-1111-1111-1111-111111111111'
);

INSERT INTO "course_offering_group" (course_offering_id, group_id)
SELECT 'd3333333-3333-3333-3333-333333333333', 'a2222222-2222-2222-2222-222222222222'
WHERE NOT EXISTS (
  SELECT 1 FROM "course_offering_group"
  WHERE course_offering_id = 'd3333333-3333-3333-3333-333333333333'
    AND group_id = 'a2222222-2222-2222-2222-222222222222'
);

INSERT INTO "exam" (
  id, course_offering_id, label, date_exam,
  coefficient_numerator, coefficient_denominator
)
SELECT 'e1111111-1111-1111-1111-111111111111',
       'd1111111-1111-1111-1111-111111111111',
       'Final SYS1', '2025-01-15 08:00:00+03', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM "exam" WHERE id = 'e1111111-1111-1111-1111-111111111111');

INSERT INTO "exam" (
  id, course_offering_id, label, date_exam,
  coefficient_numerator, coefficient_denominator
)
SELECT 'e2222222-2222-2222-2222-222222222222',
       'd2222222-2222-2222-2222-222222222222',
       'Final PROG4', '2025-06-10 08:00:00+03', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM "exam" WHERE id = 'e2222222-2222-2222-2222-222222222222');

INSERT INTO "exam" (
  id, course_offering_id, label, date_exam,
  coefficient_numerator, coefficient_denominator
)
SELECT 'e3333333-3333-3333-3333-333333333333',
       'd3333333-3333-3333-3333-333333333333',
       'Final WEB4', '2025-06-12 08:00:00+03', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM "exam" WHERE id = 'e3333333-3333-3333-3333-333333333333');

INSERT INTO "grade" (id, exam_id, student_id, value, entered_by)
SELECT 'f1111111-1111-1111-1111-111111111111',
       'e1111111-1111-1111-1111-111111111111',
       '44444444-4444-4444-4444-444444444444', 14.00,
       '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (SELECT 1 FROM "grade" WHERE id = 'f1111111-1111-1111-1111-111111111111');

INSERT INTO "grade" (id, exam_id, student_id, value, entered_by)
SELECT 'f2222222-2222-2222-2222-222222222222',
       'e2222222-2222-2222-2222-222222222222',
       '44444444-4444-4444-4444-444444444444', 16.00,
       '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (SELECT 1 FROM "grade" WHERE id = 'f2222222-2222-2222-2222-222222222222');

INSERT INTO "grade" (id, exam_id, student_id, value, entered_by)
SELECT 'f3333333-3333-3333-3333-333333333333',
       'e1111111-1111-1111-1111-111111111111',
       '55555555-5555-5555-5555-555555555555', 11.00,
       '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (SELECT 1 FROM "grade" WHERE id = 'f3333333-3333-3333-3333-333333333333');

INSERT INTO "grade" (id, exam_id, student_id, value, entered_by)
SELECT 'f4444444-4444-4444-4444-444444444444',
       'e3333333-3333-3333-3333-333333333333',
       '66666666-6666-6666-6666-666666666666', 13.50,
       '33333333-3333-3333-3333-333333333333'
WHERE NOT EXISTS (SELECT 1 FROM "grade" WHERE id = 'f4444444-4444-4444-4444-444444444444');