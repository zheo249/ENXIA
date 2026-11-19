package edu.example.studentperformance;

/**
 * Operacion que choca con el estado actual de los datos (nombre duplicado,
 * curso con matriculas...). El controlador la traduce a HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
