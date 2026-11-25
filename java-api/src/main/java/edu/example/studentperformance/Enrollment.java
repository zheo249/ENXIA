package edu.example.studentperformance;

/**
 * Representa la matricula de un estudiante en una asignatura concreta
 * junto con las horas de estudio dedicadas a ella (HU-04).
 */
public class Enrollment {
    private int subjectId;
    private String subjectName;
    private String difficulty;
    private double studyHours;

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
}
