package com.nexa.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ApiClient {
    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final ApiClient INSTANCE = new ApiClient();

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final AtomicReference<String> authToken = new AtomicReference<>();
    private volatile String baseUrl = "http://localhost:8080/api/v1";

    private ApiClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String envUrl = System.getProperty("nexa.api.base-url", System.getenv("NEXA_API_BASE_URL"));
        if (envUrl != null && !envUrl.isBlank()) {
            this.baseUrl = envUrl.endsWith("/") ? envUrl.substring(0, envUrl.length() - 1) : envUrl;
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json");

                    String token = authToken.get();
                    if (token != null && !token.isBlank() && original.header("Authorization") == null) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(requestBuilder.build());
                });

        this.client = builder.build();
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl != null) {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }
    }

    public void setAuthToken(String token) {
        this.authToken.set(token);
    }

    public String getAuthToken() {
        return this.authToken.get();
    }

    public void clearAuthToken() {
        this.authToken.set(null);
    }

    public boolean isAuthenticated() {
        return this.authToken.get() != null && !this.authToken.get().isBlank();
    }

    // --- Synchronous methods ---

    public <T> T get(String endpoint, Class<T> responseType) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .get()
                .build();
        return execute(request, responseType);
    }

    public <T> T get(String endpoint, TypeReference<T> responseType) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .get()
                .build();
        return execute(request, responseType);
    }

    public <T> T post(String endpoint, Object body, Class<T> responseType) {
        RequestBody requestBody = createJsonBody(body);
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .post(requestBody)
                .build();
        return execute(request, responseType);
    }

    public <T> T post(String endpoint, Object body, TypeReference<T> responseType) {
        RequestBody requestBody = createJsonBody(body);
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .post(requestBody)
                .build();
        return execute(request, responseType);
    }

    public <T> T patch(String endpoint, Object body, Class<T> responseType) {
        RequestBody requestBody = createJsonBody(body);
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .patch(requestBody)
                .build();
        return execute(request, responseType);
    }

    public <T> T delete(String endpoint, Class<T> responseType) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .delete()
                .build();
        return execute(request, responseType);
    }

    // --- Asynchronous methods with CompletableFuture ---

    public <T> CompletableFuture<T> getAsync(String endpoint, Class<T> responseType) {
        return executeAsync(new Request.Builder().url(buildUrl(endpoint)).get().build(), responseType);
    }

    public <T> CompletableFuture<T> getAsync(String endpoint, TypeReference<T> responseType) {
        return executeAsync(new Request.Builder().url(buildUrl(endpoint)).get().build(), responseType);
    }

    public <T> CompletableFuture<T> postAsync(String endpoint, Object body, Class<T> responseType) {
        return executeAsync(new Request.Builder().url(buildUrl(endpoint)).post(createJsonBody(body)).build(), responseType);
    }

    public <T> CompletableFuture<T> postAsync(String endpoint, Object body, TypeReference<T> responseType) {
        return executeAsync(new Request.Builder().url(buildUrl(endpoint)).post(createJsonBody(body)).build(), responseType);
    }

    public <T> CompletableFuture<T> patchAsync(String endpoint, Object body, Class<T> responseType) {
        return executeAsync(new Request.Builder().url(buildUrl(endpoint)).patch(createJsonBody(body)).build(), responseType);
    }

    public <T> CompletableFuture<T> deleteAsync(String endpoint, Class<T> responseType) {
        return executeAsync(new Request.Builder().url(buildUrl(endpoint)).delete().build(), responseType);
    }

    // --- Streaming SSE / Chunked POST Support ---

    public void streamPost(String endpoint, Object body, Consumer<String> onChunk, Runnable onComplete, Consumer<Throwable> onError) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint))
                .header("Accept", "text/event-stream, application/x-ndjson, text/plain")
                .post(createJsonBody(body))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("Streaming request failed", e);
                if (onError != null) onError.accept(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    if (onError != null) {
                        onError.accept(new ApiException(response.code(), errorBody, "HTTP_" + response.code()));
                    }
                    return;
                }

                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isBlank() || line.startsWith(":")) continue;
                            if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if ("[DONE]".equals(data)) break;
                                if (onChunk != null) onChunk.accept(data);
                            } else {
                                if (onChunk != null) onChunk.accept(line);
                            }
                        }
                    }
                    if (onComplete != null) onComplete.run();
                } catch (Exception e) {
                    log.error("Error reading stream", e);
                    if (onError != null) onError.accept(e);
                }
            }
        });
    }

    // --- Private Helpers ---

    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
    }

    private RequestBody createJsonBody(Object body) {
        if (body == null) {
            return RequestBody.create("", JSON_MEDIA_TYPE);
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            return RequestBody.create(json, JSON_MEDIA_TYPE);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize request body", e);
        }
    }

    private <T> T execute(Request request, Class<T> responseType) {
        try (Response response = client.newCall(request).execute()) {
            return handleResponse(response, responseType);
        } catch (IOException e) {
            log.error("HTTP request failed: {} {}", request.method(), request.url(), e);
            throw new ApiException("Network error: " + e.getMessage(), e);
        }
    }

    private <T> T execute(Request request, TypeReference<T> responseType) {
        try (Response response = client.newCall(request).execute()) {
            return handleResponse(response, responseType);
        } catch (IOException e) {
            log.error("HTTP request failed: {} {}", request.method(), request.url(), e);
            throw new ApiException("Network error: " + e.getMessage(), e);
        }
    }

    private <T> CompletableFuture<T> executeAsync(Request request, Class<T> responseType) {
        CompletableFuture<T> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(new ApiException("Network error: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    T result = handleResponse(response, responseType);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });
        return future;
    }

    private <T> CompletableFuture<T> executeAsync(Request request, TypeReference<T> responseType) {
        CompletableFuture<T> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(new ApiException("Network error: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    T result = handleResponse(response, responseType);
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    response.close();
                }
            }
        });
        return future;
    }

    @SuppressWarnings("unchecked")
    private <T> T handleResponse(Response response, Class<T> responseType) throws IOException {
        String body = response.body() != null ? response.body().string() : "";
        if (!response.isSuccessful()) {
            String errorMsg = extractErrorMessage(body, response.code());
            throw new ApiException(response.code(), errorMsg, "HTTP_" + response.code());
        }

        if (responseType == Void.class || responseType == void.class) {
            return null;
        }
        if (responseType == String.class) {
            return (T) body;
        }

        // Handle wrapped ApiResponse if present
        return parseApiResponse(body, responseType);
    }

    private <T> T handleResponse(Response response, TypeReference<T> responseType) throws IOException {
        String body = response.body() != null ? response.body().string() : "";
        if (!response.isSuccessful()) {
            String errorMsg = extractErrorMessage(body, response.code());
            throw new ApiException(response.code(), errorMsg, "HTTP_" + response.code());
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("data") && root.has("success")) {
                JsonNode dataNode = root.get("data");
                if (dataNode != null && !dataNode.isNull()) {
                    return objectMapper.convertValue(dataNode, responseType);
                }
            }
            return objectMapper.readValue(body, responseType);
        } catch (Exception e) {
            return objectMapper.readValue(body, responseType);
        }
    }

    private <T> T parseApiResponse(String body, Class<T> responseType) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("data") && root.has("success")) {
                boolean success = root.get("success").asBoolean(true);
                if (!success) {
                    String msg = root.has("message") ? root.get("message").asText() : "Request failed";
                    String code = root.has("errorCode") ? root.get("errorCode").asText() : "API_ERROR";
                    throw new ApiException(200, msg, code);
                }
                JsonNode dataNode = root.get("data");
                if (dataNode != null && !dataNode.isNull()) {
                    return objectMapper.treeToValue(dataNode, responseType);
                }
            }
            return objectMapper.readValue(body, responseType);
        } catch (ApiException ae) {
            throw ae;
        } catch (Exception e) {
            return objectMapper.readValue(body, responseType);
        }
    }

    private String extractErrorMessage(String body, int statusCode) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) {
                return node.get("message").asText();
            }
            if (node.has("error")) {
                return node.get("error").asText();
            }
        } catch (Exception ignored) {
        }
        return "HTTP Error " + statusCode + ": " + (body.isBlank() ? "No details provided" : body);
    }
}
