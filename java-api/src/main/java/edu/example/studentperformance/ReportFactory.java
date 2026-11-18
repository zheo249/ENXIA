package edu.example.studentperformance;

/**
 * Factoria de informes academicos (patron Factory).
 * Crea el objeto AcademicReport correspondiente al formato solicitado,
 * para que el resto de la aplicacion no instancie CsvReport ni JsonReport directamente.
 */
public class ReportFactory {

    private ReportFactory() {
    }

    public static AcademicReport create(String format, int studentId) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("El formato del informe es obligatorio");
        }
        return switch (format.trim().toLowerCase()) {
            case "csv" -> new CsvReport(studentId);
            case "json" -> new JsonReport(studentId);
            default -> throw new IllegalArgumentException(
                    "Formato de informe no admitido: " + format + " (formatos validos: csv, json)");
        };
    }
}
