package com.banksoft.useradmin.audit.controller;

import com.banksoft.useradmin.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/audit-log")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "View immutable audit records")
public class AuditController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @Operation(summary = "List audit log entries")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType) {

        String sql = "SELECT log_id, user_id, username, role_code, screen_code, action_type, " +
                "entity_type, entity_id, ip_address, status, created_at " +
                "FROM audit_log " +
                "WHERE (:action IS NULL OR action_type = :action) " +
                "ORDER BY created_at DESC " +
                "LIMIT " + size + " OFFSET " + (page * size);

        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT log_id, user_id, username, role_code, screen_code, action_type, " +
                "entity_type, entity_id, ip_address, status, created_at " +
                "FROM audit_log ORDER BY created_at DESC LIMIT ? OFFSET ?",
                size, page * size);

        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
