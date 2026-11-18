package edu.example.studentperformance;

/**
 * Representa una solicitud de informe academico de un estudiante.
 * Contiene lo comun a todos los formatos; cada subclase define lo especifico.
 * La generacion real del contenido no se hace en Java, sino en el
 * microservicio Python de informes.
 */
public abstract class AcademicReport {
    private final int studentId;

    protected AcademicReport(int studentId) {
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }

    /** Nombre del formato: "csv" o "json". */
    public abstract String getFormat();

    /** Tipo MIME para la cabecera HTTP de la descarga. */
    public abstract String getContentType();

    /** Extension del archivo descargado, sin el punto. */
    public abstract String getFileExtension();

    /** Nombre del archivo de descarga, por ejemplo: informe_estudiante_3.csv */
    public String getFileName() {
        return "informe_estudiante_" + studentId + "." + getFileExtension();
    }
}
