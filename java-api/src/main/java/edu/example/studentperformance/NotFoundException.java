package edu.example.studentperformance;

/**
 * El recurso pedido no existe. El controlador la traduce a HTTP 404.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
