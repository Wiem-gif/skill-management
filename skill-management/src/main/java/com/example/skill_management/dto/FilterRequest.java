package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterRequest {
    private String name;      // ex: "Java", "jobTitle", "matricule"
    private String operator;  // ex: "equals", "greaterThan", "lowerThan"
    private String value;     // ex: "Basic", "Expert", "QAIngenier"
}