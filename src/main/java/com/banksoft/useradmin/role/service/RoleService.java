package com.banksoft.useradmin.role.service;

import com.banksoft.useradmin.common.CbsException;
import com.banksoft.useradmin.role.dto.AssignRoleRequest;
import com.banksoft.useradmin.role.dto.CreateRoleRequest;
import com.banksoft.useradmin.role.dto.RoleResponse;
import com.banksoft.useradmin.role.entity.Role;
import com.banksoft.useradmin.role.entity.UserRole;
import com.banksoft.useradmin.role.repository.RoleRepository;
import com.banksoft.useradmin.role.repository.UserRoleRepository;
import com.banksoft.useradmin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID roleId) {
        return toResponse(roleRepository.findById(roleId)
                .orElseThrow(() -> CbsException.notFound("Role not found: " + roleId)));
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request, UUID createdBy) {
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw CbsException.conflict("Role code already exists: " + request.getRoleCode());
        }
        Role role = Role.builder()
                .roleCode(request.getRoleCode().toUpperCase())
                .roleName(request.getRoleName())
                .roleLevel(request.getRoleLevel())
                .description(request.getDescription())
                .maxTransactionAmt(request.getMaxTransactionAmt())
                .requiresMfa(request.getRequiresMfa() != null ? request.getRequiresMfa() : false)
                .sessionTimeoutMin(request.getSessionTimeoutMin() != null ? request.getSessionTimeoutMin() : 480)
                .isSystem(false)
                .createdBy(createdBy)
                .build();
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(UUID roleId, CreateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> CbsException.notFound("Role not found: " + roleId));
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw CbsException.forbidden("System roles cannot be modified");
        }
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        if (request.getMaxTransactionAmt() != null) role.setMaxTransactionAmt(request.getMaxTransactionAmt());
        if (request.getRequiresMfa() != null) role.setRequiresMfa(request.getRequiresMfa());
        if (request.getSessionTimeoutMin() != null) role.setSessionTimeoutMin(request.getSessionTimeoutMin());
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> CbsException.notFound("Role not found: " + roleId));
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw CbsException.forbidden("System roles cannot be deleted");
        }
        role.setIsActive(false);
        roleRepository.save(role);
    }

    @Transactional
    public UserRole assignRole(UUID roleId, AssignRoleRequest request, UUID assignedBy) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> CbsException.notFound("Role not found: " + roleId));
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> CbsException.notFound("User not found: " + request.getUserId()));

        UserRole existing = userRoleRepository.findByUserIdAndRoleId(request.getUserId(), roleId)
                .orElse(null);
        if (existing != null && Boolean.TRUE.equals(existing.getIsActive())) {
            throw CbsException.conflict("User already has this role");
        }

        UserRole userRole = UserRole.builder()
                .userId(request.getUserId())
                .roleId(roleId)
                .isPrimaryRole(request.isPrimaryRole())
                .assignedBy(assignedBy)
                .expiresAt(request.getExpiresAt())
                .build();
        return userRoleRepository.save(userRole);
    }

    @Transactional
    public void revokeRole(UUID roleId, UUID userId, UUID revokedBy, String reason) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> CbsException.notFound("Role assignment not found"));
        userRole.setIsActive(false);
        userRole.setRevokedBy(revokedBy);
        userRole.setRevokedAt(Instant.now());
        userRole.setRevokeReason(reason);
        userRoleRepository.save(userRole);
    }

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getRoleUsers(UUID roleId) {
        return userRoleRepository.findByRoleIdAndIsActiveTrue(roleId).stream()
                .map(ur -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("userId", ur.getUserId());
                    map.put("isPrimaryRole", ur.getIsPrimaryRole());
                    map.put("assignedAt", ur.getAssignedAt());
                    map.put("expiresAt", ur.getExpiresAt());
                    userRepository.findById(ur.getUserId()).ifPresent(u -> {
                        map.put("username", u.getUsername());
                        map.put("fullName", u.getFullName());
                        map.put("email", u.getEmail());
                    });
                    return map;
                })
                .collect(Collectors.toList());
    }

    private RoleResponse toResponse(Role role) {
        long userCount = userRoleRepository.findByRoleIdAndIsActiveTrue(role.getRoleId()).size();
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .roleLevel(role.getRoleLevel())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .maxTransactionAmt(role.getMaxTransactionAmt())
                .requiresMfa(role.getRequiresMfa())
                .sessionTimeoutMin(role.getSessionTimeoutMin())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .userCount(userCount)
                .build();
    }
}
