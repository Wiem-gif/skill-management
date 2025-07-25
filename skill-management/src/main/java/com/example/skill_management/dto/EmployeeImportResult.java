package com.example.skill_management.dto;

import com.example.skill_management.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeImportResult {
    private int rowNumber;
    private Employee employee;
    private String error;
}