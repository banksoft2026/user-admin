package com.banksoft.useradmin.permission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FieldPermRequest {
    @NotNull
    private UUID roleId;
    @NotNull
    private UUID screenId;
    @NotNull
    private UUID fieldId;
    private String visibility = "VISIBLE";
    private String editability = "READ_ONLY";
    private String maskPattern;
}
