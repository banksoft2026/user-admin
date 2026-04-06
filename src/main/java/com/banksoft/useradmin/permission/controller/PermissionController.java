package com.banksoft.useradmin.permission.controller;

import com.banksoft.useradmin.common.ApiResponse;
import com.banksoft.useradmin.permission.dto.FieldPermRequest;
import com.banksoft.useradmin.permission.dto.ScreenPermRequest;
import com.banksoft.useradmin.permission.dto.ScreenPermissionContext;
import com.banksoft.useradmin.permission.entity.*;
import com.banksoft.useradmin.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Screen and field permission management")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/modules")
    @Operation(summary = "List all modules")
    public ResponseEntity<ApiResponse<List<CbsModule>>> getModules() {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.getAllModules()));
    }

    @GetMapping("/screens")
    @Operation(summary = "List all screens")
    public ResponseEntity<ApiResponse<List<Screen>>> getScreens() {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.getAllScreens()));
    }

    @GetMapping("/screen-perms")
    @Operation(summary = "Get screen permissions for a role")
    public ResponseEntity<ApiResponse<List<RoleScreenPerm>>> getScreenPerms(
            @RequestParam UUID roleId) {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.getScreenPermsForRole(roleId)));
    }

    @PostMapping("/screen-perms")
    @Operation(summary = "Grant or update screen permission for a role")
    public ResponseEntity<ApiResponse<RoleScreenPerm>> upsertScreenPerm(
            @Valid @RequestBody ScreenPermRequest request,
            @AuthenticationPrincipal String grantedBy) {
        UUID granterId = grantedBy != null ? UUID.fromString(grantedBy) : null;
        return ResponseEntity.ok(ApiResponse.ok(permissionService.upsertScreenPerm(request, granterId)));
    }

    @PostMapping("/field-perms")
    @Operation(summary = "Set field-level permission")
    public ResponseEntity<ApiResponse<FieldPermission>> upsertFieldPerm(
            @Valid @RequestBody FieldPermRequest request,
            @AuthenticationPrincipal String grantedBy) {
        UUID granterId = grantedBy != null ? UUID.fromString(grantedBy) : null;
        return ResponseEntity.ok(ApiResponse.ok(permissionService.upsertFieldPerm(request, granterId)));
    }

    @GetMapping("/resolve")
    @Operation(summary = "Resolve full permission context for a user on a screen")
    public ResponseEntity<ApiResponse<ScreenPermissionContext>> resolve(
            @RequestParam UUID userId,
            @RequestParam UUID screenId) {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.resolvePermissions(userId, screenId)));
    }

    @GetMapping("/user-context")
    @Operation(summary = "Resolve all accessible screens for a user (for menu building)")
    public ResponseEntity<ApiResponse<List<ScreenPermissionContext>>> userContext(
            @RequestParam UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.resolveAllUserPermissions(userId)));
    }
}
