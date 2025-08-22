package com.example.skill_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class EmployeeSkillsRequest {
    private Long employeeId;

    private List<SkillLevelDTO> skills;

    @Data
    public static class SkillLevelDTO {
        private Long skillId;
        private String currentLevel;
    }
}
