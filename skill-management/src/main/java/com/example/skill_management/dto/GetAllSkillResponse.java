package com.example.skill_management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class GetAllSkillResponse {
    private Long id;

    private Long skillCategoryId;
    private String categoryName;
    private String name;
    private String description;

}
