package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillInfo {
    private Long id;
    private String name;
    private String category;
    private String currentLevel;
}
