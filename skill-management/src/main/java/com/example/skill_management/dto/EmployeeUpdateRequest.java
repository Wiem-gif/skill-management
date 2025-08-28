package com.example.skill_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeUpdateRequest {
    private String matricule;
    private String firstname;
    private String lastname;
    private String gender;
    private LocalDate birthday;
    private String cin;
    private String email;
    private String activity;
    private Long gradeId;
    private String function;
    private Integer previousExperience;
    private String hierarchicalHead;
    private LocalDate dateEntry;
    private String contractType;
    private LocalDate contractEnd;
    private String status;
    private Long jobTitleId;
}
