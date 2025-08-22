package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkillUpdateResponse {

    private int nbSuccess;
    private int nbCreatedSkills;
    private List<SkillInfo> createdSkills;
    private int nbUpdatedSkills;
    private List<SkillInfo> updatedSkills;
    private int nbFailures; // nouveau champ
    private List<FailureInfo> failureDetails; // nouveau champ

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillInfo {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailureInfo {
        private Long skillId;
        private String errorMessage;
    }
}
