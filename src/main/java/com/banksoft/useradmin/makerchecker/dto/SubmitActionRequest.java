package com.banksoft.useradmin.makerchecker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitActionRequest {
    @NotBlank(message = "Action type is required")
    private String actionType;
    @NotBlank(message = "Entity type is required")
    private String entityType;
    private String entityId;
    private UUID screenId;
    private String payloadBefore;
    @NotNull(message = "Payload after is required")
    private String payloadAfter;
    private String priority = "NORMAL";
}
