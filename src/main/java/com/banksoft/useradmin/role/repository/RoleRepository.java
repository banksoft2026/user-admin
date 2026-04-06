package com.banksoft.useradmin.role.repository;

import com.banksoft.useradmin.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleCode(String roleCode);
    boolean existsByRoleCode(String roleCode);
    List<Role> findAllByIsActiveTrueOrderByRoleCodeAsc();
}
