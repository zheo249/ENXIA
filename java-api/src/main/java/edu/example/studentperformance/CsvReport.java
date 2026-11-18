package edu.example.studentperformance;

/**
 * Solicitud de informe academico en formato CSV.
 * Solo define lo especifico del formato; el contenido lo genera el
 * microservicio Python de informes.
 */
public class CsvReport extends AcademicReport {

    public CsvReport(int studentId) {
        super(studentId);
    }

    @Override
    public String getFormat() {
        return "csv";
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }
}
