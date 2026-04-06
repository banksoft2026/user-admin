package com.banksoft.useradmin.permission.repository;

import com.banksoft.useradmin.permission.entity.ScreenField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScreenFieldRepository extends JpaRepository<ScreenField, UUID> {
    List<ScreenField> findByScreenIdOrderBySortOrder(UUID screenId);
}
