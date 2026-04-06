package com.banksoft.useradmin.user.repository;

import com.banksoft.useradmin.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:branchCode IS NULL OR u.branchCode = :branchCode) " +
           "AND (:department IS NULL OR u.department = :department)")
    Page<User> searchUsers(@Param("search") String search,
                           @Param("status") String status,
                           @Param("branchCode") String branchCode,
                           @Param("department") String department,
                           Pageable pageable);
}
