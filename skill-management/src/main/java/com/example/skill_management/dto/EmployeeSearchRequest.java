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
public class EmployeeSearchRequest {
    private List<FilterRequest> skillfilters;
    private List<FilterRequest> employeefilters;
}
