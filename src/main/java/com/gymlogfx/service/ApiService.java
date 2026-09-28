package com.gymlogfx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ApiService - Handles HTTP networking and JSON parsing.
 *
 * MULTITHREADING: Uses a dedicated thread pool (ExecutorService) for all
 * HTTP requests to avoid blocking the JavaFX Application Thread.
 * HTTP calls run on background threads; results are passed back via
 * CompletableFuture for reactive handling.
 *
 * JSON Parsing: Uses Jackson Databind (ObjectMapper) to deserialize API responses.
 */
public class ApiService {

    // THREAD POOL: Fixed pool of 4 threads for concurrent HTTP requests
    // This demonstrates Thread Pool usage for I/O-bound operations
    private static final ExecutorService HTTP_THREAD_POOL =
        Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "ApiService-HTTP-Worker");
            t.setDaemon(true); // daemon threads shut down with the JVM
            return t;
        });

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(HTTP_THREAD_POOL)  // Use our thread pool for async HTTP
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches workout quotes from a public API asynchronously.
     * Demonstrates: HTTP GET request + JSON parsing + CompletableFuture
     *
     * THREADING: Runs on HTTP_THREAD_POOL (background thread)
     * The caller receives a CompletableFuture and updates the UI on the JavaFX thread
     */
    public CompletableFuture<List<String>> fetchMotivationalQuotes() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> quotes = new ArrayList<>();
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.quotable.io/quotes/random?limit=5&tags=inspirational"))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // JSON PARSING: Parse array of quote objects using Jackson
                    JsonNode array = objectMapper.readTree(response.body());
                    for (JsonNode obj : array) {
                        String content = obj.get("content").asText();
                        String author  = obj.get("author").asText();
                        quotes.add("\"" + content + "\" \u2014 " + author);
                    }
                } else {
                    quotes.addAll(getFallbackQuotes());
                }
            } catch (Exception e) {
                System.out.println("API call failed, using fallback quotes: " + e.getMessage());
                quotes.addAll(getFallbackQuotes());
            }
            return quotes;
        }, HTTP_THREAD_POOL);
    }

    /**
     * Fetches exercise data from a public API.
     * Demonstrates: Parsing nested JSON objects + error handling
     *
     * THREADING: Runs on HTTP_THREAD_POOL via CompletableFuture
     */
    public CompletableFuture<List<String>> fetchExerciseTips() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> tips = new ArrayList<>();
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://jsonplaceholder.typicode.com/posts?_limit=5"))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // JSON PARSING: Parse array of post objects \u2192 adapt as tips
                    JsonNode array = objectMapper.readTree(response.body());
                    for (JsonNode obj : array) {
                        String title = obj.get("title").asText();
                        tips.add("\uD83D\uDCAA Tip: " + capitalize(title));
                    }
                } else {
                    tips.addAll(getDefaultTips());
                }
            } catch (Exception e) {
                System.out.println("Exercise tips API failed: " + e.getMessage());
                tips.addAll(getDefaultTips());
            }
            return tips;
        }, HTTP_THREAD_POOL);
    }

    /**
     * Creates a JavaFX Task for fetching data - demonstrates integration
     * of JavaFX concurrency with Java threading.
     *
     * THREADING: Task runs on a separate thread via Thread.start()
     * Task.updateMessage() is thread-safe for JavaFX UI updates
     */
    public Task<List<String>> createQuoteFetchTask() {
        return new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                updateMessage("Connecting to API...");
                updateProgress(0, 3);

                Thread.sleep(200); // Simulate connection time
                updateMessage("Sending HTTP GET request...");
                updateProgress(1, 3);

                List<String> quotes = fetchMotivationalQuotes().get();

                updateMessage("Parsing JSON response...");
                updateProgress(2, 3);
                Thread.sleep(100);

                updateMessage("Done!");
                updateProgress(3, 3);
                return quotes;
            }
        };
    }

    private List<String> getFallbackQuotes() {
        return List.of(
            "\"The only bad workout is the one that didn't happen.\" \u2014 Unknown",
            "\"Train insane or remain the same.\" \u2014 Jillian Michaels",
            "\"Your body can stand almost anything. It's your mind you have to convince.\" \u2014 Unknown",
            "\"Success starts with self-discipline.\" \u2014 Unknown",
            "\"No pain, no gain.\" \u2014 Jane Fonda"
        );
    }

    private List<String> getDefaultTips() {
        return List.of(
            "\uD83D\uDCAA Tip: Progressive overload is key to muscle growth",
            "\uD83D\uDCAA Tip: Ensure 7-9 hours of sleep for optimal recovery",
            "\uD83D\uDCAA Tip: Protein intake should be ~1g per pound of bodyweight",
            "\uD83D\uDCAA Tip: Warm up properly to prevent injury",
            "\uD83D\uDCAA Tip: Stay hydrated during your workout"
        );
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static void shutdownThreadPool() {
        HTTP_THREAD_POOL.shutdown();
    }
}
