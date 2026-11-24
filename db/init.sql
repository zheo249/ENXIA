-- Esquema de la base de datos de Student Performance Predictor (HU-05).
-- MySQL ejecuta este script automaticamente solo la PRIMERA vez que se crea
-- el volumen de datos. Si se modifica, hay que recrear el volumen:
--   docker compose down -v && docker compose up --build

CREATE TABLE IF NOT EXISTS students (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    study_hours DOUBLE       NOT NULL DEFAULT 0,
    CONSTRAINT chk_students_study_hours CHECK (study_hours >= 0)
);

CREATE TABLE IF NOT EXISTS subjects (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    difficulty ENUM('facil', 'media', 'dificil') NOT NULL
);

-- Matricula: un estudiante cursa una asignatura en un curso academico concreto.
-- Un mismo estudiante puede repetir asignatura en cursos distintos (HU-02),
-- por eso el curso forma parte de la clave primaria.
-- grade es NULL mientras la matricula no tenga calificacion.
CREATE TABLE IF NOT EXISTS enrollments (
    student_id    INT          NOT NULL,
    subject_id    INT          NOT NULL,
    academic_year VARCHAR(9)   NOT NULL,
    study_hours   DOUBLE       NOT NULL DEFAULT 0,
    grade         DECIMAL(4,2) NULL,
    PRIMARY KEY (student_id, subject_id, academic_year),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (id) ON DELETE CASCADE,
    CONSTRAINT chk_enrollments_study_hours CHECK (study_hours >= 0),
    CONSTRAINT chk_enrollments_grade CHECK (grade IS NULL OR (grade >= 0 AND grade <= 10)),
    CONSTRAINT chk_enrollments_academic_year CHECK (academic_year REGEXP '^[0-9]{4}/[0-9]{4}$')
);
