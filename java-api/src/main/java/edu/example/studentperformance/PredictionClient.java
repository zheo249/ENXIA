package edu.example.studentperformance;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.text.Normalizer;

/**
 * Encapsula la comunicación HTTP con el servicio de predicción escrito en
 * Python.
 */

public class PredictionClient {
    private final String serviceUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public PredictionClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        httpClient = HttpClient.newHttpClient();
        gson = new Gson();
    }

    public double requestPrediction(double studyHours, String difficulty) throws IOException, InterruptedException {
        return requestPrediction(studyHours, difficulty, null);
    }

    public double requestPrediction(
            double studyHours,
            String difficulty,
            Course course) throws IOException, InterruptedException {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("study_hours", studyHours);
        requestData.addProperty("type", normalizeDifficulty(difficulty));
        if (course != null) {
            requestData.add("course", gson.toJsonTree(course));
        }

        URI predictionUri = URI.create(serviceUrl + "/predict");
        String requestBody = gson.toJson(requestData);
        Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.uri(predictionUri);
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Prediction service returned an error: " + response.statusCode());
        }

        JsonObject responseData = gson.fromJson(response.body(), JsonObject.class);
        return responseData.get("predicted_grade").getAsDouble();
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "media";
        }
        String normalized = Normalizer.normalize(difficulty.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return switch (normalized) {
            case "easy", "facil" -> "facil";
            case "hard", "dificil" -> "dificil";
            default -> "media";
        };
    }
}