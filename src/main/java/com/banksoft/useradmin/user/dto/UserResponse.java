package com.banksoft.useradmin.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String employeeId;
    private String department;
    private String branchCode;
    private String status;
    private Boolean mfaEnabled;
    private Integer failedLoginCount;
    private Instant lockedUntil;
    private Instant lastLoginAt;
    private String lastLoginIp;
    private Boolean mustChangePassword;
    private Instant createdAt;
    private Instant updatedAt;
    private List<RoleSummary> roles;

    @Data
    @Builder
    public static class RoleSummary {
        private UUID roleId;
        private String roleCode;
        private String roleName;
        private String roleLevel;
        private boolean isPrimaryRole;
    }
}
