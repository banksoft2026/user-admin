package com.banksoft.useradmin.auth.service;

import com.banksoft.useradmin.auth.dto.*;
import com.banksoft.useradmin.auth.entity.AuthAttempt;
import com.banksoft.useradmin.auth.entity.UserSession;
import com.banksoft.useradmin.auth.repository.AuthAttemptRepository;
import com.banksoft.useradmin.auth.repository.UserSessionRepository;
import com.banksoft.useradmin.common.CbsException;
import com.banksoft.useradmin.common.JwtUtil;
import com.banksoft.useradmin.role.entity.Role;
import com.banksoft.useradmin.role.repository.RoleRepository;
import com.banksoft.useradmin.role.repository.UserRoleRepository;
import com.banksoft.useradmin.user.entity.User;
import com.banksoft.useradmin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository sessionRepository;
    private final AuthAttemptRepository attemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-expiry-ms}")
    private long refreshExpiryMs;

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    recordAttempt(request.getUsername(), ipAddress, false, "USER_NOT_FOUND");
                    return CbsException.unauthorized("Invalid credentials");
                });

        // Check if locked
        if ("LOCKED".equals(user.getStatus()) || "SUSPENDED".equals(user.getStatus())) {
            recordAttempt(request.getUsername(), ipAddress, false, "ACCOUNT_" + user.getStatus());
            throw CbsException.unauthorized("Account is " + user.getStatus().toLowerCase());
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            recordAttempt(request.getUsername(), ipAddress, false, "TEMP_LOCKED");
            throw CbsException.unauthorized("Account temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int failCount = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failCount);
            if (failCount >= 5) {
                user.setLockedUntil(Instant.now().plusSeconds(1800)); // 30 min
                user.setStatus("LOCKED");
            }
            userRepository.save(user);
            recordAttempt(request.getUsername(), ipAddress, false, "WRONG_PASSWORD");
            throw CbsException.unauthorized("Invalid credentials");
        }

        // Successful login
        user.setFailedLoginCount(0);
        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp(ipAddress);
        if ("LOCKED".equals(user.getStatus())) {
            // auto-unlock on successful login after timeout
            user.setStatus("ACTIVE");
            user.setLockedUntil(null);
        }
        userRepository.save(user);

        List<String> roles = getActiveRoleCodes(user.getUserId());

        String accessToken = jwtUtil.generateAccessToken(
                user.getUserId().toString(), user.getUsername(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId().toString());

        // Store session
        UserSession session = UserSession.builder()
                .userId(user.getUserId())
                .refreshTokenHash(hashToken(refreshToken))
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusMillis(refreshExpiryMs))
                .build();
        sessionRepository.save(session);

        recordAttempt(request.getUsername(), ipAddress, true, null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .build();
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        String hash = hashToken(request.getRefreshToken());
        UserSession session = sessionRepository.findByRefreshTokenHashAndIsActiveTrue(hash)
                .orElseThrow(() -> CbsException.unauthorized("Invalid or expired refresh token"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setIsActive(false);
            sessionRepository.save(session);
            throw CbsException.unauthorized("Refresh token expired");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> CbsException.notFound("User not found"));

        List<String> roles = getActiveRoleCodes(user.getUserId());
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getUserId().toString(), user.getUsername(), roles);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .expiresIn(900)
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = hashToken(refreshToken);
        sessionRepository.findByRefreshTokenHashAndIsActiveTrue(hash).ifPresent(session -> {
            session.setIsActive(false);
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CbsException.notFound("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw CbsException.badRequest("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
    }

    private List<String> getActiveRoleCodes(UUID userId) {
        return userRoleRepository.findActiveRolesForUser(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId())
                        .map(Role::getRoleCode).orElse(null))
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    private void recordAttempt(String username, String ip, boolean success, String reason) {
        attemptRepository.save(AuthAttempt.builder()
                .username(username)
                .ipAddress(ip)
                .success(success)
                .failureReason(reason)
                .build());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
