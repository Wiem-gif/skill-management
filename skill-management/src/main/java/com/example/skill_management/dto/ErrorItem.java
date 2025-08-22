package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorItem {
    private int row;
    private String employeeMatricule;
    private String skillName;
//    private String code;
    private String message;
}
