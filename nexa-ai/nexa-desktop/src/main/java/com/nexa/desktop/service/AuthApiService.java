package com.nexa.desktop.service;

import com.nexa.desktop.model.AuthResponseDto;
import com.nexa.desktop.model.LoginRequestDto;
import com.nexa.desktop.model.RegisterRequestDto;
import com.nexa.desktop.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class AuthApiService {
    private static final Logger log = LoggerFactory.getLogger(AuthApiService.class);
    private static final AuthApiService INSTANCE = new AuthApiService();

    private final ApiClient apiClient = ApiClient.getInstance();
    private final AtomicReference<UserDto> currentUser = new AtomicReference<>();

    private AuthApiService() {}

    public static AuthApiService getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<AuthResponseDto> login(String email, String password) {
        LoginRequestDto request = LoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        return apiClient.postAsync("/auth/login", request, AuthResponseDto.class)
                .thenApply(authResponse -> {
                    if (authResponse != null && authResponse.getAccessToken() != null) {
                        apiClient.setAuthToken(authResponse.getAccessToken());
                        if (authResponse.getUser() != null) {
                            currentUser.set(authResponse.getUser());
                        }
                    }
                    return authResponse;
                });
    }

    public CompletableFuture<AuthResponseDto> register(String email, String password, String displayName) {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .email(email)
                .password(password)
                .displayName(displayName)
                .build();

        return apiClient.postAsync("/auth/register", request, AuthResponseDto.class)
                .thenApply(authResponse -> {
                    if (authResponse != null && authResponse.getAccessToken() != null) {
                        apiClient.setAuthToken(authResponse.getAccessToken());
                        if (authResponse.getUser() != null) {
                            currentUser.set(authResponse.getUser());
                        }
                    }
                    return authResponse;
                });
    }

    public CompletableFuture<Void> logout() {
        return apiClient.postAsync("/auth/logout", null, Void.class)
                .exceptionally(ex -> {
                    log.warn("Logout remote call failed, clearing local session anyway: {}", ex.getMessage());
                    return null;
                })
                .thenAccept(v -> {
                    apiClient.clearAuthToken();
                    currentUser.set(null);
                });
    }

    public CompletableFuture<UserDto> getCurrentUser() {
        if (currentUser.get() != null) {
            return CompletableFuture.completedFuture(currentUser.get());
        }
        return apiClient.getAsync("/users/me", UserDto.class)
                .thenApply(user -> {
                    currentUser.set(user);
                    return user;
                });
    }

    public UserDto getCurrentUserCached() {
        return currentUser.get();
    }

    public void setCurrentUser(UserDto user) {
        this.currentUser.set(user);
    }

    public boolean isLoggedIn() {
        return apiClient.isAuthenticated();
    }
}
