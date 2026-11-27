package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EnrollmentController implements HttpHandler {
    private final EnrollmentService enrollmentService;
    private final Gson gson = new Gson();

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
                    sendResponse(exchange, 200, gson.toJson(enrollments));
                } else if (method.equalsIgnoreCase("POST")) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Enrollment enrollment = parseEnrollmentFromJson(body);
                    enrollmentService.createEnrollment(studentId, enrollment);
                    sendResponse(exchange, 201, "{\"message\":\"Matrícula creada con éxito\"}");
                } else {
                    sendResponse(exchange, 405, "{\"error\": \"Método no permitido\"}");
                }
            } else if (path.matches("^/api/students/\\d+/enrollments/\\d+/\\d+$")
                    && method.equalsIgnoreCase("PUT")) {
                String[] parts = path.split("/");
                int studentId = Integer.parseInt(parts[3]);
                int subjectId = Integer.parseInt(parts[5]);
                int courseId = Integer.parseInt(parts[6]);
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject request = gson.fromJson(body, JsonObject.class);
                Double grade = null;
                if (request != null && request.has("grade") && !request.get("grade").isJsonNull()) {
                    grade = request.get("grade").getAsDouble();
                }
                enrollmentService.updateGrade(studentId, subjectId, courseId, grade);
                sendResponse(exchange, 200, "{\"message\":\"Calificación actualizada\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (JsonParseException | NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Datos JSON o identificadores no válidos\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (ConflictException e) {
            sendResponse(exchange, 409, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (DataAccessException e) {
            sendResponse(exchange, 503, "{\"error\":\"Base de datos no disponible\"}");
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
        return gson.fromJson(json, Enrollment.class);
    }
}