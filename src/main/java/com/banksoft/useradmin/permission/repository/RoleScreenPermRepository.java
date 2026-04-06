package com.banksoft.useradmin.permission.repository;

import com.banksoft.useradmin.permission.entity.RoleScreenPerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleScreenPermRepository extends JpaRepository<RoleScreenPerm, UUID> {
    List<RoleScreenPerm> findByRoleIdAndIsActiveTrue(UUID roleId);
    List<RoleScreenPerm> findByScreenIdAndIsActiveTrue(UUID screenId);
    Optional<RoleScreenPerm> findByRoleIdAndScreenId(UUID roleId, UUID screenId);
}
