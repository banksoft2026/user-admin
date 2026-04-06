package com.banksoft.useradmin.user.controller;

import com.banksoft.useradmin.common.ApiResponse;
import com.banksoft.useradmin.user.dto.CreateUserRequest;
import com.banksoft.useradmin.user.dto.UpdateUserRequest;
import com.banksoft.useradmin.user.dto.UserResponse;
import com.banksoft.useradmin.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD for bank operators")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Search and list users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> result = userService.searchUsers(search, status, branchCode, department,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(userId)));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal String createdBy) {
        UUID creatorId = createdBy != null ? UUID.fromString(createdBy) : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created successfully",
                        userService.createUser(request, creatorId)));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUser(userId, request)));
    }

    @PostMapping("/{userId}/lock")
    @Operation(summary = "Lock user account")
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.changeStatus(userId, "LOCKED", "Admin lock")));
    }

    @PostMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user account")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.changeStatus(userId, "ACTIVE", "Admin unlock")));
    }

    @PostMapping("/{userId}/suspend")
    @Operation(summary = "Suspend user account")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.changeStatus(userId, "SUSPENDED", "Admin suspend")));
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "Activate user account")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.changeStatus(userId, "ACTIVE", "Admin activate")));
    }

    @DeleteMapping("/{userId}/sessions")
    @Operation(summary = "Revoke all user sessions")
    public ResponseEntity<ApiResponse<Void>> revokeSessions(@PathVariable UUID userId) {
        userService.revokeAllSessions(userId);
        return ResponseEntity.ok(ApiResponse.ok("All sessions revoked", null));
    }
}
