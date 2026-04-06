package com.banksoft.useradmin.role.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RoleResponse {
    private UUID roleId;
    private String roleCode;
    private String roleName;
    private String roleLevel;
    private String description;
    private Boolean isSystem;
    private BigDecimal maxTransactionAmt;
    private Boolean requiresMfa;
    private Integer sessionTimeoutMin;
    private Boolean isActive;
    private Instant createdAt;
    private long userCount;
}
