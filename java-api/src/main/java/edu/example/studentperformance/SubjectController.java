package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SubjectController implements HttpHandler {

    private final SubjectService service;
    private final Gson gson;

    public SubjectController(SubjectService service) {
        this.service = service;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                String json = gson.toJson(service.getAllSubjects());
                sendJson(exchange, 200, json);
                return;
            }

            if ("POST".equals(method)) {
                InputStreamReader reader = new InputStreamReader(
                        exchange.getRequestBody(),
                        StandardCharsets.UTF_8
                );

                Subject subject = gson.fromJson(reader, Subject.class);
                Subject created = service.createSubject(subject);

                sendJson(exchange, 201, gson.toJson(created));
                return;
            }

            sendError(exchange, 405, "Método no permitido");

        } catch (JsonParseException exception) {
            sendError(exchange, 400, "Los datos enviados no son válidos");

        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, exception.getMessage());

        } catch (DataAccessException exception) {
            exception.printStackTrace();
            sendError(exchange, 503, "Database is unavailable");
        }
    }

    private void sendError(
            HttpExchange exchange,
            int status,
            String message) throws IOException {

        String json = "{\"error\":\"" + message + "\"}";
        sendJson(exchange, status, json);
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            String json) throws IOException {

        byte[] body = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=utf-8"
        );

        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
