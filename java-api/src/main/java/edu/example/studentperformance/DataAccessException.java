package edu.example.studentperformance;

/**
 * Error al acceder a la base de datos (conexion caida, consulta fallida...).
 * Es una excepcion no comprobada para no obligar a las capas intermedias
 * (servicio) a declararla; el controlador la traduce a una respuesta HTTP 503.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
