package edu.example.studentperformance;

/**
 * Representa un curso académico y sus fechas de inicio y fin.
 */
public class Course {
    private int id;
    private String academicYear;
    private String startDate;
    private String endDate;

    public Course() {
    }

    public Course(String academicYear, String startDate, String endDate) {
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Course(int id, String academicYear, String startDate, String endDate) {
        this.id = id;
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
