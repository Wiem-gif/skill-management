package com.example.skill_management.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("employee")
public class Employee {


    @Column("matricule")
    private String matricule;

    @Column("firstname")
    private String firstname;

    @Column("lastname")
    private String lastname;

    @Column("gender")
    private String gender;

    @Column("birthday")
    private LocalDate birthday;

    @Column("cin")
    private String cin;

    @Column("email")
    private String email;

    @Column("activity")
    private String activity;

    @Column("grade")
    private String grade;

    @Column("function")
    private String function;

    @Column("previous_experience")
    private Integer previousExperience;

    @Column("hierarchical_head")
    private String hierarchicalHead;

    @Column("date_entry")
    private LocalDate dateEntry;

    @Column("contract_type")
    private String contractType;

    @Column("contract_end")
    private LocalDate contractEnd;

    @Column("status")
    private String status;
}
