package com.banksoft.useradmin.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String fullName;
    private String employeeId;
    private String department;
    private String branchCode;
}
