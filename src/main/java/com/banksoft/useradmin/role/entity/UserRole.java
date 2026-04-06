package com.banksoft.useradmin.role.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @Id
    @UuidGenerator
    @Column(name = "user_role_id", updatable = false, nullable = false)
    private UUID userRoleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "is_primary_role", nullable = false)
    @Builder.Default
    private Boolean isPrimaryRole = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private Instant assignedAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_by")
    private UUID revokedBy;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason")
    private String revokeReason;
}
