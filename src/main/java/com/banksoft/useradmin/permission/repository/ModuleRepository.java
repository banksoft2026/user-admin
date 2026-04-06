package com.banksoft.useradmin.permission.repository;

import com.banksoft.useradmin.permission.entity.CbsModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<CbsModule, UUID> {
    List<CbsModule> findAllByIsActiveTrueOrderBySortOrder();
}
