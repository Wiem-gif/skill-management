package com.example.skill_management.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("permission")
public class PermissionEntity {
    @Id
    private Long id;

    private String name;
}
