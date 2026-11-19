package edu.example.studentperformance;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Reglas de negocio de los cursos academicos (HU-01).
 */
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course getCourse(int id) {
        Course course = repository.findById(id);
        if (course == null) {
            throw new NotFoundException("Curso no encontrado");
        }
        return course;
    }

    public Course createCourse(Course course) {
        validate(course);
        if (repository.existsByAcademicYear(course.getAcademicYear(), 0)) {
            throw new ConflictException("Ya existe un curso académico con ese nombre");
        }
        return repository.create(course);
    }

    public Course updateCourse(int id, Course course) {
        getCourse(id);
        validate(course);
        if (repository.existsByAcademicYear(course.getAcademicYear(), id)) {
            throw new ConflictException("Ya existe un curso académico con ese nombre");
        }
        repository.update(id, course);
        course.setId(id);
        return course;
    }

    public void deleteCourse(int id) {
        getCourse(id);
        if (repository.hasEnrollments(id)) {
            throw new ConflictException("No se puede eliminar: el curso tiene matrículas asociadas");
        }
        repository.delete(id);
    }

    private void validate(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("El curso no puede estar vacío");
        }
        if (course.getAcademicYear() == null || course.getAcademicYear().isBlank()) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio");
        }
        course.setAcademicYear(course.getAcademicYear().trim());

        LocalDate start = parseDate(course.getStartDate(), "inicio");
        LocalDate end = parseDate(course.getEndDate(), "finalización");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la de inicio");
        }
        // Se normaliza al formato ISO (yyyy-MM-dd) que espera MySQL.
        course.setStartDate(start.toString());
        course.setEndDate(end.toString());
    }

    private LocalDate parseDate(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La fecha de " + label + " es obligatoria");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fecha de " + label + " debe tener el formato aaaa-mm-dd");
        }
    }
}
