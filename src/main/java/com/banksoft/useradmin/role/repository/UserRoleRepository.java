package com.banksoft.useradmin.role.repository;

import com.banksoft.useradmin.role.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserIdAndIsActiveTrue(UUID userId);

    List<UserRole> findByRoleIdAndIsActiveTrue(UUID roleId);

    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId AND ur.isActive = true " +
           "AND (ur.expiresAt IS NULL OR ur.expiresAt > CURRENT_TIMESTAMP)")
    List<UserRole> findActiveRolesForUser(@Param("userId") UUID userId);
}
