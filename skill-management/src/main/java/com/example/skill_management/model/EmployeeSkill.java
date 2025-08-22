package com.example.skill_management.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("employee_skill")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSkill {



    @Column("employee_id")
    private Long employeeId;



    @Column("skill_id")
    private Long skillId;
    @Column("current_level")
    private String currentLevel;
}
