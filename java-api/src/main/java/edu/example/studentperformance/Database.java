package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la configuracion y la apertura de conexiones JDBC con MySQL.
 *
 * La configuracion se lee de variables de entorno. En Docker Compose las
 * define el propio compose.yaml; en desarrollo local se usan los valores por
 * defecto, que apuntan al MySQL publicado por Compose en el puerto 3307.
 */
public class Database {
    private final AppConfig config;

    public Database() {
        this.config = AppConfig.getInstance();
    }

    /**
     * Abre una conexion nueva. Quien la pide debe cerrarla (try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getDbUrl(),
                config.getDbUser(),
                config.getDbPassword());
    }
}