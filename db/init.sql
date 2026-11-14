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

-- Matricula: relacion N:M entre estudiantes y asignaturas.
-- Cada fila guarda las horas de estudio de ese estudiante en esa asignatura.
CREATE TABLE IF NOT EXISTS enrollments (
    student_id  INT    NOT NULL,
    subject_id  INT    NOT NULL,
    study_hours DOUBLE NOT NULL DEFAULT 0,
    PRIMARY KEY (student_id, subject_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (id) ON DELETE CASCADE,
    CONSTRAINT chk_enrollments_study_hours CHECK (study_hours >= 0)
);
