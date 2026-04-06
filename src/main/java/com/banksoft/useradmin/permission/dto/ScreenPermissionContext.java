package com.banksoft.useradmin.permission.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ScreenPermissionContext {
    private UUID userId;
    private UUID screenId;
    private String screenCode;
    private boolean canAccess;
    private String accessLevel;
    private List<FieldContext> fields;

    @Data
    @Builder
    public static class FieldContext {
        private UUID fieldId;
        private String fieldCode;
        private String fieldName;
        private String visibility;
        private String editability;
        private String maskPattern;
    }
}
