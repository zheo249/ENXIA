package edu.example.studentperformance;

/**
 * Representa la matricula de un estudiante en una asignatura concreta
 * junto con las horas de estudio dedicadas a ella (HU-04).
 */
public class Enrollment {
    private int subjectId;
    private int courseId;
    private String courseName;
    private Double grade;
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

    public Enrollment(int subjectId, int courseId, String courseName,
                      String subjectName, String difficulty, double studyHours, Double grade) {
        this(subjectId, subjectName, difficulty, studyHours);
        this.courseId = courseId;
        this.courseName = courseName;
        this.grade = grade;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
}
