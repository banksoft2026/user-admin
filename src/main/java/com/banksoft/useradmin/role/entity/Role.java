package com.banksoft.useradmin.role.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @UuidGenerator
    @Column(name = "role_id", updatable = false, nullable = false)
    private UUID roleId;

    @Column(name = "role_code", nullable = false, unique = true, length = 30)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "role_level", nullable = false, length = 20)
    private String roleLevel;

    @Column(name = "description")
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "max_transaction_amt", precision = 18, scale = 2)
    private BigDecimal maxTransactionAmt;

    @Column(name = "requires_mfa", nullable = false)
    @Builder.Default
    private Boolean requiresMfa = false;

    @Column(name = "session_timeout_min", nullable = false)
    @Builder.Default
    private Integer sessionTimeoutMin = 480;

    @Column(name = "ip_allowlist", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String ipAllowlist;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
