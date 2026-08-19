-- Seed démo HEI (ids fixes pour Postman / tests)
-- Mot de passe en clair "password" pour tous les users (pas encore de login BCrypt).
-- Pour Basic Auth plus tard : remplacer password_hash par un hash BCrypt.

-- UUIDs de référence :
-- promotion  : 11111111-1111-1111-1111-111111111111
-- admin      : 22222222-2222-2222-2222-222222222222
-- teacher    : 33333333-3333-3333-3333-333333333333
-- student EL : 44444444-4444-4444-4444-444444444444  (STD24001)
-- student EL : 55555555-5555-5555-5555-555555555555  (STD24002)
-- student TN : 66666666-6666-6666-6666-666666666666  (STD24003)

INSERT INTO "promotion" (id, code, entry_calendar_year, expected_graduation_year)
VALUES ('11111111-1111-1111-1111-111111111111', 'K', 2024, 2027)
ON CONFLICT DO NOTHING;

INSERT INTO "app_user" (
    id, email, password_hash, first_name, last_name, role
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'admin@hei.school',
    'password',
    'Ada',
    'Admin',
    'ADMIN'
) ON CONFLICT (email) DO NOTHING;

INSERT INTO "app_user" (
    id, email, password_hash, first_name, last_name, role, teacher_ref
) VALUES (
    '33333333-3333-3333-3333-333333333333',
    'teacher@hei.school',
    'password',
    'Tiana',
    'Teacher',
    'TEACHER',
    'TCH001'
) ON CONFLICT (email) DO NOTHING;

INSERT INTO "app_user" (
    id, email, password_hash, first_name, last_name, role,
    student_ref, promotion_id, specialization, enrolled_at
) VALUES
(
    '44444444-4444-4444-4444-444444444444',
    'std24001@hei.school',
    'password',
    'Jean',
    'Rakoto',
    'STUDENT',
    'STD24001',
    '11111111-1111-1111-1111-111111111111',
    'EL',
    '2024-09-01'
),
(
    '55555555-5555-5555-5555-555555555555',
    'std24002@hei.school',
    'password',
    'Marie',
    'Rabe',
    'STUDENT',
    'STD24002',
    '11111111-1111-1111-1111-111111111111',
    'EL',
    '2024-09-01'
),
(
    '66666666-6666-6666-6666-666666666666',
    'std24003@hei.school',
    'password',
    'Paul',
    'Andria',
    'STUDENT',
    'STD24003',
    '11111111-1111-1111-1111-111111111111',
    'TN',
    '2024-09-01'
)
ON CONFLICT (email) DO NOTHING;

-- Groupes L3
INSERT INTO "class_group" (id, promotion_id, ref, academic_level, specialization)
VALUES
(
    'a1111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    'G-EL',
    'L3',
    'EL'
),
(
    'a2222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'G-TN',
    'L3',
    'TN'
)
ON CONFLICT DO NOTHING;

-- Memberships actives
INSERT INTO "student_group_membership" (id, student_id, group_id, start_date, end_date)
VALUES
(
    'b1111111-1111-1111-1111-111111111111',
    '44444444-4444-4444-4444-444444444444',
    'a1111111-1111-1111-1111-111111111111',
    '2024-09-01',
    NULL
),
(
    'b2222222-2222-2222-2222-222222222222',
    '55555555-5555-5555-5555-555555555555',
    'a1111111-1111-1111-1111-111111111111',
    '2024-09-01',
    NULL
),
(
    'b3333333-3333-3333-3333-333333333333',
    '66666666-6666-6666-6666-666666666666',
    'a2222222-2222-2222-2222-222222222222',
    '2024-09-01',
    NULL
)
ON CONFLICT DO NOTHING;

-- Cours (S4+ peut être EL/TN ; S1-S3 = COMMUN uniquement)
INSERT INTO "course" (id, ref, title, credits, semester_number, specialization)
VALUES
(
    'c1111111-1111-1111-1111-111111111111',
    'SYS1',
    'Systemes 1',
    6,
    1,
    'COMMUN'
),
(
    'c2222222-2222-2222-2222-222222222222',
    'PROG4',
    'Programmation 4',
    6,
    4,
    'EL'
),
(
    'c3333333-3333-3333-3333-333333333333',
    'WEB4',
    'Web 4',
    5,
    4,
    'TN'
)
ON CONFLICT (ref) DO NOTHING;

-- Offerings 2025
INSERT INTO "course_offering" (id, course_id, calendar_year)
VALUES
(
    'd1111111-1111-1111-1111-111111111111',
    'c1111111-1111-1111-1111-111111111111',
    2025
),
(
    'd2222222-2222-2222-2222-222222222222',
    'c2222222-2222-2222-2222-222222222222',
    2025
),
(
    'd3333333-3333-3333-3333-333333333333',
    'c3333333-3333-3333-3333-333333333333',
    2025
)
ON CONFLICT DO NOTHING;

INSERT INTO "course_offering_teacher" (course_offering_id, teacher_id)
VALUES
('d1111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333'),
('d2222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333'),
('d3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333')
ON CONFLICT DO NOTHING;

INSERT INTO "course_offering_group" (course_offering_id, group_id)
VALUES
('d1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111'),
('d1111111-1111-1111-1111-111111111111', 'a2222222-2222-2222-2222-222222222222'),
('d2222222-2222-2222-2222-222222222222', 'a1111111-1111-1111-1111-111111111111'),
('d3333333-3333-3333-3333-333333333333', 'a2222222-2222-2222-2222-222222222222')
ON CONFLICT DO NOTHING;

-- Examens
INSERT INTO "exam" (
    id, course_offering_id, label, date_exam,
    coefficient_numerator, coefficient_denominator
) VALUES
(
    'e1111111-1111-1111-1111-111111111111',
    'd1111111-1111-1111-1111-111111111111',
    'Final SYS1',
    '2025-01-15 08:00:00+03',
    1,
    1
),
(
    'e2222222-2222-2222-2222-222222222222',
    'd2222222-2222-2222-2222-222222222222',
    'Final PROG4',
    '2025-06-10 08:00:00+03',
    1,
    1
),
(
    'e3333333-3333-3333-3333-333333333333',
    'd3333333-3333-3333-3333-333333333333',
    'Final WEB4',
    '2025-06-12 08:00:00+03',
    1,
    1
)
ON CONFLICT DO NOTHING;

-- Notes (pas assez de crédits pour diplôme — OK pour tester grades/averages)
INSERT INTO "grade" (id, exam_id, student_id, value, entered_by)
VALUES
(
    'f1111111-1111-1111-1111-111111111111',
    'e1111111-1111-1111-1111-111111111111',
    '44444444-4444-4444-4444-444444444444',
    14.00,
    '33333333-3333-3333-3333-333333333333'
),
(
    'f2222222-2222-2222-2222-222222222222',
    'e2222222-2222-2222-2222-222222222222',
    '44444444-4444-4444-4444-444444444444',
    16.00,
    '33333333-3333-3333-3333-333333333333'
),
(
    'f3333333-3333-3333-3333-333333333333',
    'e1111111-1111-1111-1111-111111111111',
    '55555555-5555-5555-5555-555555555555',
    11.00,
    '33333333-3333-3333-3333-333333333333'
),
(
    'f4444444-4444-4444-4444-444444444444',
    'e3333333-3333-3333-3333-333333333333',
    '66666666-6666-6666-6666-666666666666',
    13.50,
    '33333333-3333-3333-3333-333333333333'
)
ON CONFLICT DO NOTHING;