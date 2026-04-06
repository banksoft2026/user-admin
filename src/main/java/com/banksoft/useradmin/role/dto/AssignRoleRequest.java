package com.banksoft.useradmin.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AssignRoleRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;
    private boolean isPrimaryRole;
    private Instant expiresAt;
}
