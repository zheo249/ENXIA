package edu.example.studentperformance;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EnrollmentController implements HttpHandler {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService, HttpServer server) {
        this.enrollmentService = enrollmentService;
        server.createContext("/api/students", this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if (path.matches("^/api/students/\\d+/enrollments$")) {
                int studentId = extractStudentId(path);

                if (method.equalsIgnoreCase("GET")) {
                    List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(studentId);
                    sendResponse(exchange, 200, toJson(enrollments));
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Enrollment enrollment = parseEnrollmentFromJson(body);
                    enrollmentService.createEnrollment(studentId, enrollment);
                    sendResponse(exchange, 201, "{\"message\": \"Matrícula creada con éxito\"}");
                } else {
                    sendResponse(exchange, 405, "{\"error\": \"Método no permitido\"}");
                }
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private int extractStudentId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[3]);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Enrollment parseEnrollmentFromJson(String json) {
        Enrollment e = new Enrollment();
        // Ajusta según los campos que reciba el JSON de la HU-02
        return e;
    }

    private String toJson(List<Enrollment> enrollments) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < enrollments.size(); i++) {
            Enrollment e = enrollments.get(i);
            sb.append("{")
              .append("\"subjectId\":").append(e.getSubjectId())
              .append("}");
            if (i < enrollments.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}