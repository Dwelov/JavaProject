package com.expensetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String authToken;
    private static String userFullName;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setUserFullName(String name) {
        userFullName = name;
    }

    public static String getUserFullName() {
        return userFullName;
    }

    public static <T> T post(String endpoint, Object body, Class<T> responseType) throws Exception {
        String jsonBody = mapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        return executeRequest(builder.build(), responseType);
    }

    public static <T> T get(String endpoint, Class<T> responseType) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET();
        
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        return executeRequest(builder.build(), responseType);
    }

    public static void delete(String endpoint) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(TIMEOUT)
                .DELETE();
        
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        executeRequest(builder.build(), Void.class);
    }

    private static <T> T executeRequest(HttpRequest request, Class<T> responseType) throws Exception {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 400) {
                handleErrorResponse(response);
            }
            
            if (responseType == Void.class || response.body() == null || response.body().trim().isEmpty()) {
                return null;
            }
            
            return mapper.readValue(response.body(), responseType);
        } catch (java.net.ConnectException | java.net.http.HttpConnectTimeoutException | java.net.UnknownHostException e) {
            throw new RuntimeException("Could not connect to the server. Please ensure the backend is running at " + BASE_URL);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Network error: " + e.getMessage());
        }
    }

    private static void handleErrorResponse(HttpResponse<String> response) throws Exception {
        String responseBody = response.body();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new RuntimeException("Server error: HTTP " + response.statusCode());
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> errorResponse = mapper.readValue(responseBody, Map.class);
            String errorMsg = null;
            
            if (errorResponse.containsKey("details")) {
                Object details = errorResponse.get("details");
                if (details instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> fieldErrors = (Map<String, String>) details;
                    errorMsg = String.join("; ", fieldErrors.values());
                } else {
                    errorMsg = details.toString();
                }
            } else if (errorResponse.containsKey("error")) {
                errorMsg = (String) errorResponse.get("error");
            } else if (errorResponse.containsKey("message")) {
                errorMsg = (String) errorResponse.get("message");
            }
            
            throw new RuntimeException(errorMsg != null ? errorMsg : "Error: HTTP " + response.statusCode());
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Server error (" + response.statusCode() + "): " + responseBody);
        }
    }
}
