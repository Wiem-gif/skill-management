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
public class EmployeeSkillImportResponse {

    private int nbSuccess;
    private int nbCreatedSkills;
    private List<SkillInfo> createdSkills;

    private int nbFailures;
    private List<FailureDetail> failureDetails;
}