package com.banksoft.useradmin.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "screens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screen {

    @Id
    @UuidGenerator
    @Column(name = "screen_id", updatable = false, nullable = false)
    private UUID screenId;

    @Column(name = "screen_code", nullable = false, unique = true, length = 30)
    private String screenCode;

    @Column(name = "screen_name", nullable = false, length = 100)
    private String screenName;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "route_path", nullable = false, unique = true, length = 200)
    private String routePath;

    @Column(name = "screen_type", nullable = false, length = 20)
    private String screenType;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
