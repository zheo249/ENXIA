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
    private final String url;
    private final String user;
    private final String password;

    public Database() {
        this.url = env("DB_URL",
                "jdbc:mysql://localhost:3307/studentdb"
                        + "?useUnicode=true&characterEncoding=utf8"
                        + "&allowPublicKeyRetrieval=true&useSSL=false");
        this.user = env("DB_USER", "student");
        this.password = env("DB_PASSWORD", "student_password");
    }

    /**
     * Abre una conexion nueva. Quien la pide debe cerrarla (try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
