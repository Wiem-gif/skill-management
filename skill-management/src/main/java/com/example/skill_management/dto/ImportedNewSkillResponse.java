package com.example.skill_management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportedNewSkillResponse {
    private String name;
    private String category;
}
