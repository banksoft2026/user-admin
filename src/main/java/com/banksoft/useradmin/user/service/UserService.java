package com.banksoft.useradmin.user.service;

import com.banksoft.useradmin.common.CbsException;
import com.banksoft.useradmin.role.entity.Role;
import com.banksoft.useradmin.role.entity.UserRole;
import com.banksoft.useradmin.role.repository.RoleRepository;
import com.banksoft.useradmin.role.repository.UserRoleRepository;
import com.banksoft.useradmin.user.dto.CreateUserRequest;
import com.banksoft.useradmin.user.dto.UpdateUserRequest;
import com.banksoft.useradmin.user.dto.UserResponse;
import com.banksoft.useradmin.user.entity.User;
import com.banksoft.useradmin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String search, String status,
                                          String branchCode, String department,
                                          Pageable pageable) {
        return userRepository.searchUsers(search, status, branchCode, department, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CbsException.notFound("User not found: " + userId));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> CbsException.notFound("User not found: " + username));
        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID createdBy) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw CbsException.conflict("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw CbsException.conflict("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername().toLowerCase())
                .email(request.getEmail().toLowerCase())
                .fullName(request.getFullName())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .branchCode(request.getBranchCode())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .mustChangePassword(true)
                .status("ACTIVE")
                .createdBy(createdBy)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CbsException.notFound("User not found: " + userId));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw CbsException.conflict("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmployeeId() != null) user.setEmployeeId(request.getEmployeeId());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getBranchCode() != null) user.setBranchCode(request.getBranchCode());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeStatus(UUID userId, String newStatus, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CbsException.notFound("User not found: " + userId));
        user.setStatus(newStatus);
        if ("ACTIVE".equals(newStatus)) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void revokeAllSessions(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> CbsException.notFound("User not found: " + userId));
        // Sessions are managed in AuthService; flag user for session invalidation
    }

    private UserResponse toResponse(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndIsActiveTrue(user.getUserId());
        List<UserResponse.RoleSummary> roleSummaries = userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleId())
                        .map(role -> UserResponse.RoleSummary.builder()
                                .roleId(role.getRoleId())
                                .roleCode(role.getRoleCode())
                                .roleName(role.getRoleName())
                                .roleLevel(role.getRoleLevel())
                                .isPrimaryRole(Boolean.TRUE.equals(ur.getIsPrimaryRole()))
                                .build())
                        .orElse(null))
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .employeeId(user.getEmployeeId())
                .department(user.getDepartment())
                .branchCode(user.getBranchCode())
                .status(user.getStatus())
                .mfaEnabled(user.getMfaEnabled())
                .failedLoginCount(user.getFailedLoginCount())
                .lockedUntil(user.getLockedUntil())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .mustChangePassword(user.getMustChangePassword())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleSummaries)
                .build();
    }
}
