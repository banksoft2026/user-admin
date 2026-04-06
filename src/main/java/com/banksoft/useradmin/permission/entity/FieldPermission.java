package com.banksoft.useradmin.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "field_permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldPermission {

    @Id
    @UuidGenerator
    @Column(name = "field_perm_id", updatable = false, nullable = false)
    private UUID fieldPermId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "screen_id", nullable = false)
    private UUID screenId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private String visibility = "VISIBLE";

    @Column(name = "editability", nullable = false, length = 20)
    @Builder.Default
    private String editability = "READ_ONLY";

    @Column(name = "mask_pattern", length = 50)
    private String maskPattern;

    @Column(name = "granted_by", nullable = false)
    private UUID grantedBy;

    @Column(name = "granted_at", nullable = false)
    @Builder.Default
    private Instant grantedAt = Instant.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
