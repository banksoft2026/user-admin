package com.banksoft.useradmin.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthAttempt {

    @Id
    @UuidGenerator
    @Column(name = "attempt_id", updatable = false, nullable = false)
    private UUID attemptId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @Column(name = "attempted_at", nullable = false)
    @Builder.Default
    private Instant attemptedAt = Instant.now();
}
