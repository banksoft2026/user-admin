package com.banksoft.useradmin.role.controller;

import com.banksoft.useradmin.common.ApiResponse;
import com.banksoft.useradmin.role.dto.AssignRoleRequest;
import com.banksoft.useradmin.role.dto.CreateRoleRequest;
import com.banksoft.useradmin.role.dto.RoleResponse;
import com.banksoft.useradmin.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Manage roles and role assignments")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "List all roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getAllRoles()));
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID roleId) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getRole(roleId)));
    }

    @PostMapping
    @Operation(summary = "Create a new custom role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            @AuthenticationPrincipal String createdBy) {
        UUID creatorId = createdBy != null ? UUID.fromString(createdBy) : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Role created", roleService.createRole(request, creatorId)));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.updateRole(roleId, request)));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Deactivate role (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.ok("Role deactivated", null));
    }

    @PostMapping("/{roleId}/assign")
    @Operation(summary = "Assign role to a user")
    public ResponseEntity<ApiResponse<Object>> assignRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignRoleRequest request,
            @AuthenticationPrincipal String assignedBy) {
        UUID assignerId = assignedBy != null ? UUID.fromString(assignedBy) : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Role assigned", roleService.assignRole(roleId, request, assignerId)));
    }

    @DeleteMapping("/{roleId}/revoke/{userId}")
    @Operation(summary = "Revoke role from user")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
            @PathVariable UUID roleId,
            @PathVariable UUID userId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal String revokedBy) {
        UUID revokerId = revokedBy != null ? UUID.fromString(revokedBy) : null;
        roleService.revokeRole(roleId, userId, revokerId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Role revoked", null));
    }

    @GetMapping("/{roleId}/users")
    @Operation(summary = "List users with this role")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRoleUsers(@PathVariable UUID roleId) {
        roleService.getRole(roleId); // validate exists
        var userRoles = roleService.getRoleUsers(roleId);
        return ResponseEntity.ok(ApiResponse.ok(userRoles));
    }
}
