package com.banksoft.useradmin.init;

import com.banksoft.useradmin.role.entity.UserRole;
import com.banksoft.useradmin.role.repository.RoleRepository;
import com.banksoft.useradmin.role.repository.UserRoleRepository;
import com.banksoft.useradmin.user.entity.User;
import com.banksoft.useradmin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Creating default admin user...");
            User admin = User.builder()
                    .username("admin")
                    .email("admin@banksoft.internal")
                    .fullName("System Administrator")
                    .employeeId("EMP-0001")
                    .department("IT")
                    .status("ACTIVE")
                    .passwordHash(passwordEncoder.encode("Admin@2026"))
                    .mustChangePassword(false)
                    .mfaEnabled(false)
                    .failedLoginCount(0)
                    .build();
            User savedAdmin = userRepository.save(admin);
            log.info("Admin user created with ID: {}", savedAdmin.getUserId());

            roleRepository.findByRoleCode("ADMIN").ifPresent(role -> {
                if (userRoleRepository.findByUserIdAndRoleId(savedAdmin.getUserId(), role.getRoleId()).isEmpty()) {
                    UserRole userRole = UserRole.builder()
                            .userId(savedAdmin.getUserId())
                            .roleId(role.getRoleId())
                            .isPrimaryRole(true)
                            .isActive(true)
                            .assignedBy(savedAdmin.getUserId())
                            .build();
                    userRoleRepository.save(userRole);
                    log.info("ADMIN role assigned to admin user");
                }
            });
        } else {
            log.info("Admin user already exists, skipping initialization.");
        }
    }
}
