package com.banksoft.useradmin.permission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ScreenPermRequest {
    @NotNull
    private UUID roleId;
    @NotNull
    private UUID screenId;
    private boolean canAccess;
    private String accessLevel = "READ";
}
