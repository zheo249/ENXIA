package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * API REST de cursos academicos (HU-01).
 *
 * GET    /courses       -> listar
 * GET    /courses/{id}  -> consultar
 * POST   /courses       -> crear
 * PUT    /courses/{id}  -> modificar
 * DELETE /courses/{id}  -> eliminar (409 si tiene matriculas)
 */
public class CourseController implements HttpHandler {

    private final CourseService service;
    private final Gson gson = new Gson();

    public CourseController(CourseService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            Integer id = extractId(exchange.getRequestURI().getPath());

            if ("GET".equals(method)) {
                if (id == null) {
                    sendJson(exchange, 200, gson.toJson(service.getAllCourses()));
                } else {
                    sendJson(exchange, 200, gson.toJson(service.getCourse(id)));
                }
                return;
            }

            if ("POST".equals(method) && id == null) {
                Course created = service.createCourse(readBody(exchange));
                sendJson(exchange, 201, gson.toJson(created));
                return;
            }

            if ("PUT".equals(method) && id != null) {
                Course updated = service.updateCourse(id, readBody(exchange));
                sendJson(exchange, 200, gson.toJson(updated));
                return;
            }

            if ("DELETE".equals(method) && id != null) {
                service.deleteCourse(id);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            sendError(exchange, 405, "Método no permitido");

        } catch (NumberFormatException exception) {
            sendError(exchange, 400, "El identificador no es válido");
        } catch (JsonParseException exception) {
            sendError(exchange, 400, "Los datos enviados no son válidos");
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, exception.getMessage());
        } catch (NotFoundException exception) {
            sendError(exchange, 404, exception.getMessage());
        } catch (ConflictException exception) {
            sendError(exchange, 409, exception.getMessage());
        } catch (DataAccessException exception) {
            exception.printStackTrace();
            sendError(exchange, 503, "Database is unavailable");
        }
    }

    private Course readBody(HttpExchange exchange) {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        return gson.fromJson(reader, Course.class);
    }

    /** "/courses" -> null; "/courses/3" -> 3. */
    private Integer extractId(String path) {
        String rest = path.substring("/courses".length());
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }
        if (rest.isEmpty()) {
            return null;
        }
        return Integer.valueOf(rest);
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("error", message);
        sendJson(exchange, status, gson.toJson(body));
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
