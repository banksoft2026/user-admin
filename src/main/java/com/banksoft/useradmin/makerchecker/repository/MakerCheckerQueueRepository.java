package com.banksoft.useradmin.makerchecker.repository;

import com.banksoft.useradmin.makerchecker.entity.MakerCheckerQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MakerCheckerQueueRepository extends JpaRepository<MakerCheckerQueue, UUID> {

    @Query("SELECT q FROM MakerCheckerQueue q WHERE " +
           "(:status IS NULL OR q.status = :status) " +
           "AND (:actionType IS NULL OR q.actionType = :actionType) " +
           "AND (:makerId IS NULL OR q.makerUserId = :makerId) " +
           "ORDER BY q.submittedAt DESC")
    Page<MakerCheckerQueue> findFiltered(@Param("status") String status,
                                          @Param("actionType") String actionType,
                                          @Param("makerId") UUID makerId,
                                          Pageable pageable);

    long countByStatus(String status);
}
