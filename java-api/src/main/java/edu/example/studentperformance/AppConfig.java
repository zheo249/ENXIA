
package edu.example.studentperformance;

import java.io.InputStream;
import java.util.Properties;

/**
 * Gestiona la configuracion centralizada de la aplicacion.
 * Implementa el patron Singleton.
 */
public final class AppConfig {

    // garantiza una unica instancia de AppConfig
    private static class Holder {
        private static final AppConfig INSTANCE = new AppConfig();
    }

    // parametros de conexion a MySQL
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    // direcciones de los microservicios Python
    private final String predictionServiceUrl;
    private final String reportServiceUrl;
    private final Properties properties = new Properties();

    /**
     * Constructor privado para impedir crear instancias desde otras clases.
     */
    private AppConfig() {

        loadProperties();

        // configuracion de MySQL
        this.dbUrl = get(
                "DB_URL",
                "jdbc:mysql://localhost:3307/studentdb"
                        + "?useUnicode=true&characterEncoding=utf8"
                        + "&allowPublicKeyRetrieval=true&useSSL=false");

        this.dbUser = get("DB_USER", "student");
        this.dbPassword = get("DB_PASSWORD", "studentpass");

        // configuracion de los microservicios
        this.predictionServiceUrl = get(
                "PYTHON_SERVICE_URL",
                "http://localhost:8000");

        this.reportServiceUrl = get(
                "REPORT_SERVICE_URL",
                "http://localhost:8001");
    }

    // Devuelve la unica instancia de AppConfig.
    
    public static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    // Carga el archivo application.properties, si existe.
    
    private void loadProperties() {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input != null) {
                properties.load(input);
            }

        } catch (Exception e) {
            System.err.println(
                    "Error al cargar application.properties: " + e.getMessage());
        }
    }

    /**
     * Obtiene un valor siguiendo este orden:
     * 1. Variable de entorno.
     * 2. Archivo application.properties.
     * 3. Valor por defecto.
     */
    private String get(String key, String defaultValue) {

        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    // get de mysql
    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    // get de los microservicios

    public String getPredictionServiceUrl() {
        return predictionServiceUrl;
    }

    public String getReportServiceUrl() {
        return reportServiceUrl;
    }
}

