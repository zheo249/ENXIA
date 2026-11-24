package edu.example.studentperformance;

/**
 * Representa la matricula de un estudiante en una asignatura concreta,
 * en un curso academico, con las horas de estudio dedicadas (HU-04)
 * y su calificacion, que puede estar pendiente (HU-02).
 */
public class Enrollment {
    private int subjectId;
    private String subjectName;
    private String difficulty;
    private double studyHours;
    private String academicYear; // formato "2025/2026"
    private Double grade;        // null = sin calificar

    public Enrollment() {
    }

    public Enrollment(int subjectId, double studyHours) {
        this.subjectId = subjectId;
        this.studyHours = studyHours;
    }

    public Enrollment(int subjectId, String subjectName, String difficulty, double studyHours) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.difficulty = difficulty;
        this.studyHours = studyHours;
    }

    public Enrollment(int subjectId, String subjectName, String difficulty,
                      double studyHours, String academicYear, Double grade) {
        this(subjectId, subjectName, difficulty, studyHours);
        this.academicYear = academicYear;
        this.grade = grade;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public double getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(double studyHours) {
        this.studyHours = studyHours;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
}