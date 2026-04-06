package com.banksoft.useradmin.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Role code is required")
    private String roleCode;

    @NotBlank(message = "Role name is required")
    private String roleName;

    @NotBlank(message = "Role level is required")
    @Pattern(regexp = "ADMIN|CHECKER|MAKER|VIEWER", message = "Role level must be ADMIN, CHECKER, MAKER, or VIEWER")
    private String roleLevel;

    private String description;
    private BigDecimal maxTransactionAmt;
    private Boolean requiresMfa;
    private Integer sessionTimeoutMin;
}
