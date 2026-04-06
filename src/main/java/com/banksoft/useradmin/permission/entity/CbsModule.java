package com.banksoft.useradmin.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "modules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CbsModule {

    @Id
    @UuidGenerator
    @Column(name = "module_id", updatable = false, nullable = false)
    private UUID moduleId;

    @Column(name = "module_code", nullable = false, unique = true, length = 30)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "parent_module_id")
    private UUID parentModuleId;

    @Column(name = "route_prefix", length = 100)
    private String routePrefix;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
