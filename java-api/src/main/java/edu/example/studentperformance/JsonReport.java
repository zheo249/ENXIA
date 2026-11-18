package edu.example.studentperformance;

/**
 * Solicitud de informe academico en formato JSON.
 * Solo define lo especifico del formato; el contenido lo genera el
 * microservicio Python de informes.
 */
public class JsonReport extends AcademicReport {

    public JsonReport(int studentId) {
        super(studentId);
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
