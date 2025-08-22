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
public class EmployeeFiltersResponse {

    private Long employeeId;
    private String firstname;
    private String lastname;
    private String matricule;


    private List<SkillInfo> skills;

}