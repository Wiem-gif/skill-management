package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBasicInfoDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String matricule;
    private String jobTitle;
}
