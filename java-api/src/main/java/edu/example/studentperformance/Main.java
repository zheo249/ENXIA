package edu.example.studentperformance;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Punto de entrada que crea los objetos de la aplicación y arranca el servidor HTTP.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        // Las dependencias se conectan de forma explícita para mostrar la relación entre capas.
        Database database = new Database();
        StudentRepository repository = new StudentRepository(database);
        SubjectRepository subjectRepository = new SubjectRepository(database);
        CourseRepository courseRepository = new CourseRepository(database);

        // en desarrollo local se utiliza localhost.
        String predictionServiceUrl = System.getenv("PYTHON_SERVICE_URL");
        if (predictionServiceUrl == null) {
            predictionServiceUrl = "http://localhost:8000";
        } else if (predictionServiceUrl.isBlank()) {
            predictionServiceUrl = "http://localhost:8000";
        }
        PredictionClient predictionClient = new PredictionClient(predictionServiceUrl);
        StudentService service = new StudentService(repository, subjectRepository, courseRepository, predictionClient);
        StudentController controller = new StudentController(service);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/students", controller);
        
        SubjectService subjectService = new SubjectService(subjectRepository);
        SubjectController subjectController = new SubjectController(subjectService);
        server.createContext("/subjects", subjectController);
        
        CourseService courseService = new CourseService(courseRepository);
        server.createContext("/courses", new CourseController(courseService));

        // --- GESTIÓN DE MATRÍCULAS E HISTORIAL (HU-02 Tarea 2) ---
        EnrollmentService enrollmentService = new EnrollmentService(repository);
        new EnrollmentController(enrollmentService, server);

        server.createContext("/", new StaticFileHandler());
        server.start();

        System.out.println("Student Performance Predictor is running at http://localhost:8080");
    }
}