package edu.example.studentperformance;

import java.util.List;

public class EnrollmentService {
    private final StudentRepository studentRepository;

    public EnrollmentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Enrollment> getStudentEnrollments(int studentId) {
        return studentRepository.findEnrollmentsByStudentId(studentId);
    }

    public void createEnrollment(int studentId, Enrollment enrollment) {
        if (enrollment.getStudyHours() < 0) {
            throw new IllegalArgumentException("Las horas de estudio no pueden ser negativas.");
        }
        // Añadir la matrícula recuperando al alumno actual y actualizándolo
        Student student = studentRepository.findById(studentId);
        if (student != null) {
            if (student.getEnrollments() == null) {
                student.setEnrollments(new java.util.ArrayList<>());
            }
            student.getEnrollments().add(enrollment);
            studentRepository.update(studentId, student);
        }
    }
}