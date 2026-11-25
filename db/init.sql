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

-- Nueva tabla para la HU-01 de la practica 7: Gestión de cursos académicos
CREATE TABLE IF NOT EXISTS courses (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    CONSTRAINT chk_courses_dates CHECK (end_date > start_date)
);

-- Matricula: relacion N:M entre estudiantes y asignaturas.
-- Cada fila guarda las horas de estudio de ese estudiante en esa asignatura.
-- Modificación de la tabla existente para HU-01 y HU-02
CREATE TABLE IF NOT EXISTS enrollments (
    student_id  INT    NOT NULL,
    subject_id  INT    NOT NULL,
    course_id   INT    NOT NULL, -- Nueva columna para el curso
    study_hours DOUBLE NOT NULL DEFAULT 0,
    grade       DOUBLE NULL,     -- Nueva columna para la nota (HU-02)
    PRIMARY KEY (student_id, subject_id, course_id), -- Permite repetir asignatura en distinto curso
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE RESTRICT, -- Impide borrar un curso si tiene matrículas (HU-01)
    CONSTRAINT chk_enrollments_study_hours CHECK (study_hours >= 0),
    CONSTRAINT chk_enrollments_grade CHECK (grade >= 0 AND grade <= 100) -- La nota debe estar entre 0 y 100 (HU-02)
);
