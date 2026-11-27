package edu.example.studentperformance;

import java.util.List;

public class EnrollmentService {
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(StudentRepository studentRepository,
                             SubjectRepository subjectRepository,
                             CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    public List<Enrollment> getStudentEnrollments(int studentId) {
        if (studentRepository.findById(studentId) == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        return studentRepository.findEnrollmentsByStudentId(studentId);
    }

    public void createEnrollment(int studentId, Enrollment enrollment) {
        if (enrollment.getStudyHours() < 0) {
            throw new IllegalArgumentException("Las horas de estudio no pueden ser negativas.");
        }
        if (enrollment.getSubjectId() <= 0 || enrollment.getCourseId() <= 0) {
            throw new IllegalArgumentException("La asignatura y el curso académico son obligatorios.");
        }
        validateGrade(enrollment.getGrade());
        if (subjectRepository.findById(enrollment.getSubjectId()) == null) {
            throw new NotFoundException("Asignatura no encontrada");
        }
        if (courseRepository.findById(enrollment.getCourseId()) == null) {
            throw new NotFoundException("Curso académico no encontrado");
        }

        Student student = studentRepository.findById(studentId);
        if (student == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }

        for (Enrollment existing : student.getEnrollments()) {
            if (existing.getSubjectId() == enrollment.getSubjectId()
                    && existing.getCourseId() == enrollment.getCourseId()) {
                throw new ConflictException("La matrícula ya existe para esa asignatura y curso académico");
            }
        }

        student.getEnrollments().add(enrollment);
        studentRepository.update(studentId, student);
    }

    public void updateGrade(int studentId, int subjectId, int courseId, Double grade) {
        if (studentRepository.findById(studentId) == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        validateGrade(grade);
        if (!studentRepository.updateEnrollmentGrade(studentId, subjectId, courseId, grade)) {
            throw new NotFoundException("Matrícula no encontrada");
        }
    }

    private void validateGrade(Double grade) {
        if (grade != null && (grade.isNaN() || grade < 0 || grade > 10)) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 10");
        }
    }
}