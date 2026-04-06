package com.banksoft.useradmin.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "screen_fields")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenField {

    @Id
    @UuidGenerator
    @Column(name = "field_id", updatable = false, nullable = false)
    private UUID fieldId;

    @Column(name = "screen_id", nullable = false)
    private UUID screenId;

    @Column(name = "field_code", nullable = false, length = 50)
    private String fieldCode;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_type", nullable = false, length = 20)
    private String fieldType;

    @Column(name = "is_sensitive", nullable = false)
    @Builder.Default
    private Boolean isSensitive = false;

    @Column(name = "default_visible", nullable = false)
    @Builder.Default
    private Boolean defaultVisible = true;

    @Column(name = "default_editable", nullable = false)
    @Builder.Default
    private Boolean defaultEditable = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
