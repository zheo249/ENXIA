package edu.example.studentperformance;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contiene las reglas de negocio y coordina el repositorio con el servicio de predicción.
 */
public class StudentService {
    private final StudentRepository repository;
    private final SubjectRepository subjectRepository;
    private final PredictionClient predictionClient;

    public StudentService(StudentRepository repository, SubjectRepository subjectRepository, PredictionClient predictionClient) {
        this.repository = repository;
        this.subjectRepository = subjectRepository;
        this.predictionClient = predictionClient;
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Student getStudent(int id) {
        return repository.findById(id);
    }

    public Student createStudent(Student student) {
        if (!isValid(student)) {
            return null;
        }
        return repository.create(student);
    }

    public Student updateStudent(int id, Student student) {
        if (repository.findById(id) == null) {
            return null;
        }
        if (!isValid(student)) {
            return null;
        }
        return repository.update(id, student);
    }

    public boolean deleteStudent(int id) {
        return repository.delete(id);
    }
    
    /**
     * Predice la nota para una asignatura concreta en la que esté matriculado el estudiante.
     */
    public double predictGradeForSubject(int studentId, int subjectId) throws IOException, InterruptedException {
        Student student = repository.findById(studentId);
        if (student == null) {
            return -1;
        }
        Subject subject = subjectRepository.findById(subjectId);
        if (subject == null) {
            return -1;
        }

        double hours = student.getStudyHours();
        if (student.getEnrollments() != null) {
            for (Enrollment enrollment : student.getEnrollments()) {
                if (enrollment.getSubjectId() == subjectId) {
                    hours = enrollment.getStudyHours();
                    break;
                }
            }
        }

        return predictionClient.requestPrediction(hours, subject.getDifficulty());
    }

    /**
     * Devuelve la predicción de todas las asignaturas matriculadas del estudiante.
     */
    public List<com.google.gson.JsonObject> predictAllSubjects(int studentId) throws IOException, InterruptedException {
        Student student = repository.findById(studentId);
        if (student == null) {
            return null;
        }

        List<com.google.gson.JsonObject> results = new java.util.ArrayList<>();
        if (student.getEnrollments() != null && !student.getEnrollments().isEmpty()) {
            for (Enrollment enrollment : student.getEnrollments()) {
                Subject subject = subjectRepository.findById(enrollment.getSubjectId());
                if (subject != null) {
                    double grade = predictionClient.requestPrediction(
                            enrollment.getStudyHours(),
                            subject.getDifficulty()
                    );
                    com.google.gson.JsonObject item = new com.google.gson.JsonObject();
                    item.addProperty("subjectId", subject.getId());
                    item.addProperty("subjectName", subject.getName());
                    item.addProperty("difficulty", subject.getDifficulty());
                    item.addProperty("studyHours", enrollment.getStudyHours());
                    item.addProperty("predictedGrade", grade);
                    results.add(item);
                }
            }
        }
        return results;
    }

    /**
     * Mantiene compatibilidad con GET /students/{id}/prediction.
     */
    public double predictGrade(int id) throws IOException, InterruptedException {
        Student student = repository.findById(id);
        if (student == null) {
            return -1;
        }

        if (student.getEnrollments() != null && !student.getEnrollments().isEmpty()) {
            Enrollment first = student.getEnrollments().get(0);
            Subject subject = subjectRepository.findById(first.getSubjectId());
            String difficulty = (subject != null) ? subject.getDifficulty() : "media";
            return predictionClient.requestPrediction(first.getStudyHours(), difficulty);
        }

        return predictionClient.requestPrediction(student.getStudyHours(), "media");
    }
    /*
    public double predictGrade(int id) throws IOException, InterruptedException {
        Student student = repository.findById(id);
        if (student == null) {
            return -1;
        }

        double hoursToPredict = student.getStudyHours();
        String difficulty = "media";

        if (student.getEnrollments() != null && !student.getEnrollments().isEmpty()) {
            Enrollment firstEnrollment = student.getEnrollments().get(0);
            hoursToPredict = firstEnrollment.getStudyHours();
            Subject subject = subjectRepository.findById(firstEnrollment.getSubjectId());
            if (subject != null && subject.getDifficulty() != null) {
                difficulty = subject.getDifficulty();
            }
        }

        return predictionClient.requestPrediction(hoursToPredict, difficulty);
    }
    */
    /**
     * Valida nombre, horas generales y cada matricula (asignatura existente, no repetida y horas >= 0).
     */
    private boolean isValid(Student student) {
        if (student == null) {
            return false;
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return false;
        }
        if (Double.isNaN(student.getStudyHours()) || student.getStudyHours() < 0) {
            return false;
        }
        if (student.getEnrollments() != null) {
            Set<Integer> seenSubjects = new HashSet<>();
            for (Enrollment enrollment : student.getEnrollments()) {
                if (enrollment == null) {
                    return false;
                }
                if (enrollment.getSubjectId() <= 0 || !seenSubjects.add(enrollment.getSubjectId())) {
                    return false;
                }
                if (Double.isNaN(enrollment.getStudyHours()) || enrollment.getStudyHours() < 0) {
                    return false;
                }
                if (subjectRepository.findById(enrollment.getSubjectId()) == null) {
                    return false;
                }
            }
        }
        return true;
    }
}
