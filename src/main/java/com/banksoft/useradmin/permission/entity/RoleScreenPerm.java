package com.banksoft.useradmin.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role_screen_perms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleScreenPerm {

    @Id
    @UuidGenerator
    @Column(name = "perm_id", updatable = false, nullable = false)
    private UUID permId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "screen_id", nullable = false)
    private UUID screenId;

    @Column(name = "can_access", nullable = false)
    @Builder.Default
    private Boolean canAccess = false;

    @Column(name = "access_level", nullable = false, length = 20)
    @Builder.Default
    private String accessLevel = "READ";

    @Column(name = "granted_by", nullable = false)
    private UUID grantedBy;

    @Column(name = "granted_at", nullable = false)
    @Builder.Default
    private Instant grantedAt = Instant.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
