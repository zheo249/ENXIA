package edu.example.studentperformance;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Punto de entrada que crea los objetos de la aplicacion y arranca el servidor HTTP.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Obtenemos la configuracion centralizada.
        AppConfig config = AppConfig.getInstance();
        // Las dependencias se conectan de forma explicita.
        Database database = new Database();
        StudentRepository repository = new StudentRepository(database);
        SubjectRepository subjectRepository = new SubjectRepository(database);

        // Obtenemos la direccion del microservicio de predicciones.
        String predictionServiceUrl = config.getPredictionServiceUrl();
        PredictionClient predictionClient = new PredictionClient(predictionServiceUrl);
        StudentService service = new StudentService(repository, subjectRepository, predictionClient);
        StudentController controller = new StudentController(service);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/students", controller);

        SubjectService subjectService = new SubjectService(subjectRepository);
        SubjectController subjectController = new SubjectController(subjectService);
        
        server.createContext("/subjects", subjectController);
        server.createContext("/", new StaticFileHandler());
        server.start();

        System.out.println("Student Performance Predictor is running at http://localhost:8080");
    }
}
