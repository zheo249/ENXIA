
-- HU-02: matriculas historicas y calificaciones.
-- Las matriculas existentes se asignan al curso 2025/2026.

ALTER TABLE enrollments
    ADD COLUMN academic_year VARCHAR(9) NOT NULL DEFAULT '2025/2026' AFTER subject_id,
    ADD COLUMN grade DECIMAL(4,2) NULL,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (student_id, subject_id, academic_year),
    ADD CONSTRAINT chk_enrollments_grade
        CHECK (grade IS NULL OR (grade >= 0 AND grade <= 10)),
    ADD CONSTRAINT chk_enrollments_academic_year
        CHECK (academic_year REGEXP '^[0-9]{4}/[0-9]{4}$');

-- A partir de ahora, el curso debe indicarse explícitamente.
ALTER TABLE enrollments
    ALTER COLUMN academic_year DROP DEFAULT;
