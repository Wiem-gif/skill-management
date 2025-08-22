package com.example.skill_management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("job_title")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobTitle {
    @Id
    private Long id;
    private String name;
    private String description;
}
