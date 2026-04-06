package com.banksoft.useradmin.auth.repository;

import com.banksoft.useradmin.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findByRefreshTokenHashAndIsActiveTrue(String refreshTokenHash);
    List<UserSession> findByUserIdAndIsActiveTrue(UUID userId);
}
