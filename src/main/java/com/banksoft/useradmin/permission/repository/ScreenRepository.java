package com.banksoft.useradmin.permission.repository;

import com.banksoft.useradmin.permission.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScreenRepository extends JpaRepository<Screen, UUID> {
    List<Screen> findAllByIsActiveTrueOrderBySortOrder();
    List<Screen> findByModuleIdAndIsActiveTrue(UUID moduleId);
    Optional<Screen> findByScreenCode(String screenCode);
}
