package com.banksoft.useradmin.permission.repository;

import com.banksoft.useradmin.permission.entity.FieldPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldPermissionRepository extends JpaRepository<FieldPermission, UUID> {
    List<FieldPermission> findByRoleIdAndScreenIdAndIsActiveTrue(UUID roleId, UUID screenId);
    Optional<FieldPermission> findByRoleIdAndScreenIdAndFieldId(UUID roleId, UUID screenId, UUID fieldId);
}
