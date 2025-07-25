package com.example.skill_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("skill_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategory {
    @Id
    private Long id;
    private String name;
}