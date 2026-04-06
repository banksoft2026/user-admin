package com.banksoft.useradmin.auth.repository;

import com.banksoft.useradmin.auth.entity.AuthAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthAttemptRepository extends JpaRepository<AuthAttempt, UUID> {
    Page<AuthAttempt> findByUsernameOrderByAttemptedAtDesc(String username, Pageable pageable);
}
