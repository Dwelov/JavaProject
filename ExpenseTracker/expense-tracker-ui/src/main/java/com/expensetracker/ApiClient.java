package com.expensetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static String authToken;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static <T> T post(String endpoint, Object body, Class<T> responseType) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), responseType);
    }

    public static void delete(String endpoint) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE();
        
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = builder.build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static <T> T get(String endpoint, Class<T> responseType) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET();
        
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), responseType);
    }
}
