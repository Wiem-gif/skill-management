package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillsResponse {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String matricule;
    private String jobTitle;

    private List<SkillInfo> skills;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private Long skillId;
        private String skillName;
        private String skillCategory;
        private String currentLevel;
    }
}
