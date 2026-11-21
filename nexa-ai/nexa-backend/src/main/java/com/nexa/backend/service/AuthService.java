package com.nexa.backend.service;

import com.nexa.backend.dto.request.LoginRequest;
import com.nexa.backend.dto.request.RegisterRequest;
import com.nexa.backend.dto.response.AuthResponse;
import com.nexa.backend.dto.response.UserResponse;
import com.nexa.backend.entity.RefreshToken;
import com.nexa.backend.entity.Role;
import com.nexa.backend.entity.User;
import com.nexa.backend.entity.UserSettings;
import com.nexa.backend.repository.RefreshTokenRepository;
import com.nexa.backend.repository.RoleRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.constants.AppConstants;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import com.nexa.security.jwt.JwtTokenProvider;
import com.nexa.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new NexaException(ErrorCode.USER_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByName(AppConstants.ROLE_USER)
                .orElseThrow(() -> new NexaException(ErrorCode.INTERNAL_ERROR, "Default role not found"));

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .roles(Set.of(userRole))
                .enabled(true)
                .emailVerified(false)
                .build();

        UserSettings settings = UserSettings.builder()
                .user(user)
                .build();
        user.setSettings(settings);

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            return generateAuthResponse(user);
        } catch (BadCredentialsException e) {
            throw new NexaException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenStr)
                .orElseThrow(() -> new NexaException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new NexaException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        // Revoke old token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String userId) {
        try {
            var uuid = java.util.UUID.fromString(userId);
            refreshTokenRepository.revokeAllUserTokens(uuid);
        } catch (Exception e) {
            log.warn("Error during logout for user {}: {}", userId, e.getMessage());
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse(AppConstants.ROLE_USER);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId().toString(), user.getEmail(), role);
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpirationMs() / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .expiresIn(900) // 15 minutes
                .user(userResponse)
                .build();
    }

    public UserResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = new java.util.HashSet<>();
        user.getRoles().forEach(r -> roleNames.add(r.getName()));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.isEnabled())
                .roles(roleNames)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
