package com.banksoft.useradmin.makerchecker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maker_checker_queue")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakerCheckerQueue {

    @Id
    @UuidGenerator
    @Column(name = "queue_id", updatable = false, nullable = false)
    private UUID queueId;

    @Column(name = "maker_user_id", nullable = false)
    private UUID makerUserId;

    @Column(name = "checker_user_id")
    private UUID checkerUserId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "screen_id")
    private UUID screenId;

    @Column(name = "payload_before", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payloadBefore;

    @Column(name = "payload_after", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payloadAfter;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private String priority = "NORMAL";

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private Instant submittedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    @Builder.Default
    private Instant expiresAt = Instant.now().plusSeconds(86400);

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "execution_error")
    private String executionError;
}
