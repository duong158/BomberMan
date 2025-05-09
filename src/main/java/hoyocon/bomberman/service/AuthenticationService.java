package hoyocon.bomberman.service;

import hoyocon.bomberman.api.entity.LoginRequest;
import hoyocon.bomberman.api.entity.LoginResponse;
import hoyocon.bomberman.api.entity.RegisterRequest;
import hoyocon.bomberman.api.entity.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthenticationService {
    private final String baseUrl = "http://localhost:8080/api/auth";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Lưu trữ thông tin người dùng đã đăng nhập
    private static User currentUser = null;
    private static String authToken = null;

    // Đăng nhập
    public CompletableFuture<Boolean> login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        try {
            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            if (response.statusCode() == 200) {
                                LoginResponse loginResponse = objectMapper.readValue(
                                        response.body(), LoginResponse.class);
                                currentUser = (User) loginResponse.getUser();
                                return true;
                            }
                            return false;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    });
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    // Đăng ký
    public CompletableFuture<Boolean> register(String username, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);

        try {
            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        return response.statusCode() == 200;
                    });
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        authToken = null;
    }
}