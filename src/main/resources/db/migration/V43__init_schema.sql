

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ---------------------------------------------------------------------
-- app_user : table plate pour STUDENT / TEACHER / ADMIN (cf. AppUser.java)
-- ---------------------------------------------------------------------
CREATE TABLE "app_user" (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    role              VARCHAR(10)  NOT NULL CHECK (role IN ('STUDENT','TEACHER','ADMIN')),

    -- Renseigné uniquement si role = STUDENT
    student_ref       VARCHAR(10)  UNIQUE CHECK (student_ref ~ '^STD[A-Za-z0-9-]+$'),
    promotion_id      UUID,   -- FK ajoutée plus bas (promotion créée après)
    specialization    VARCHAR(10) CHECK (specialization IN ('EL','TN')),
    enrolled_at       DATE,

    -- Renseigné uniquement si role = TEACHER
    teacher_ref       VARCHAR(20) UNIQUE,

    creation_datetime TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_deleted        BOOLEAN     NOT NULL DEFAULT false
);

-- ---------------------------------------------------------------------
-- promotion (cohortes K, J, H, N...)
-- ---------------------------------------------------------------------
CREATE TABLE "promotion" (
    id                        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code                      VARCHAR(5) NOT NULL,
    entry_calendar_year       INT NOT NULL,
    expected_graduation_year  INT NOT NULL,
    creation_datetime         TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_deleted                BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (code, entry_calendar_year)
);

ALTER TABLE "app_user"
    ADD CONSTRAINT fk_app_user_promotion FOREIGN KEY (promotion_id) REFERENCES "promotion"(id);

-- ---------------------------------------------------------------------
-- class_group
-- ---------------------------------------------------------------------
CREATE TABLE "class_group" (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    promotion_id      UUID NOT NULL REFERENCES "promotion"(id),
    ref               VARCHAR(10) NOT NULL,
    academic_level    VARCHAR(2)  NOT NULL CHECK (academic_level IN ('L1','L2','L3')),
    specialization    VARCHAR(10) NOT NULL CHECK (specialization IN ('COMMUN','EL','TN')),
    creation_datetime TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_deleted        BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (promotion_id, ref, academic_level),
    CHECK (academic_level <> 'L1' OR specialization = 'COMMUN')
);

-- ---------------------------------------------------------------------
-- student_group_membership (historique, jamais soft-deleted)
-- ---------------------------------------------------------------------
CREATE TABLE "student_group_membership" (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id  UUID NOT NULL REFERENCES "app_user"(id),
    group_id    UUID NOT NULL REFERENCES "class_group"(id),
    start_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date    DATE,
    CHECK (end_date IS NULL OR end_date >= start_date)
);
CREATE UNIQUE INDEX ux_student_active_group
    ON "student_group_membership" (student_id)
    WHERE end_date IS NULL;

-- ---------------------------------------------------------------------
-- course
-- ---------------------------------------------------------------------
CREATE TABLE "course" (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ref               VARCHAR(20) NOT NULL UNIQUE,
    title             VARCHAR(255) NOT NULL,
    credits           INT NOT NULL CHECK (credits > 0),
    semester_number   INT NOT NULL CHECK (semester_number BETWEEN 1 AND 6),
    specialization    VARCHAR(10) NOT NULL CHECK (specialization IN ('COMMUN','EL','TN')),
    creation_datetime TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_deleted        BOOLEAN NOT NULL DEFAULT false,
    CHECK (semester_number > 3 OR specialization = 'COMMUN') -- S1/S2/S3 = tronc commun
);

-- ---------------------------------------------------------------------
-- course_offering + tables de jointure teachers/groups
-- ---------------------------------------------------------------------
CREATE TABLE "course_offering" (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id         UUID NOT NULL REFERENCES "course"(id),
    calendar_year     INT NOT NULL,
    creation_datetime TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_deleted        BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (course_id, calendar_year)
);

CREATE TABLE "course_offering_teacher" (
    course_offering_id UUID NOT NULL REFERENCES "course_offering"(id) ON DELETE CASCADE,
    teacher_id          UUID NOT NULL REFERENCES "app_user"(id),
    PRIMARY KEY (course_offering_id, teacher_id)
);

CREATE TABLE "course_offering_group" (
    course_offering_id UUID NOT NULL REFERENCES "course_offering"(id) ON DELETE CASCADE,
    group_id            UUID NOT NULL REFERENCES "class_group"(id),
    PRIMARY KEY (course_offering_id, group_id)
);

-- ---------------------------------------------------------------------
-- exam (coefficient en fraction, cf. Exam.java + Apache Commons Fraction)
-- ---------------------------------------------------------------------
CREATE TABLE "exam" (
    id                     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_offering_id     UUID NOT NULL REFERENCES "course_offering"(id) ON DELETE CASCADE,
    label                  VARCHAR(100) NOT NULL,
    date_exam              TIMESTAMPTZ NOT NULL,
    coefficient_numerator  INT NOT NULL CHECK (coefficient_numerator > 0),
    coefficient_denominator INT NOT NULL CHECK (coefficient_denominator > 0)
);

-- ---------------------------------------------------------------------
-- grade + grade_history
-- ---------------------------------------------------------------------
CREATE TABLE "grade" (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exam_id     UUID NOT NULL REFERENCES "exam"(id) ON DELETE CASCADE,
    student_id  UUID NOT NULL REFERENCES "app_user"(id),
    value       NUMERIC(4,2) NOT NULL CHECK (value BETWEEN 0 AND 20),
    entered_by  UUID NOT NULL REFERENCES "app_user"(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (exam_id, student_id)
);

CREATE TABLE "grade_history" (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    grade_id        UUID NOT NULL REFERENCES "grade"(id) ON DELETE CASCADE,
    previous_value  NUMERIC(4,2),
    new_value       NUMERIC(4,2) NOT NULL,
    reason          TEXT NOT NULL,
    modified_by     UUID NOT NULL REFERENCES "app_user"(id),
    modified_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- transcript_request
-- ---------------------------------------------------------------------
CREATE TABLE "transcript_request" (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id     UUID NOT NULL REFERENCES "app_user"(id),
    requested_by   UUID NOT NULL REFERENCES "app_user"(id),
    status         VARCHAR(12) NOT NULL DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED')),
    s3_key         VARCHAR(500),
    email_sent_to  VARCHAR(255),
    requested_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at   TIMESTAMPTZ
);

-- ---------------------------------------------------------------------
-- graduate_export
-- ---------------------------------------------------------------------
CREATE TABLE "graduate_export" (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    promotion_id    UUID NOT NULL REFERENCES "promotion"(id),
    specialization  VARCHAR(10) NOT NULL CHECK (specialization IN ('EL','TN')),
    generated_by    UUID NOT NULL REFERENCES "app_user"(id),
    status          VARCHAR(12) NOT NULL DEFAULT 'READY'
                        CHECK (status IN ('READY','FAILED')),
    s3_key          VARCHAR(500),
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);