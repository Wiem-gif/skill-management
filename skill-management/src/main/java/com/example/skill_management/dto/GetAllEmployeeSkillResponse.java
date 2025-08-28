package com.example.skill_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class GetAllEmployeeSkillResponse {
    private Long employeeId;
    private Long skillId;
    private String skillName;
    private String currentLevel;
}
