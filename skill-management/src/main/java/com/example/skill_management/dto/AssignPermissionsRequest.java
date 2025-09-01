package com.example.skill_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignPermissionsRequest {
    private String role;
    private List<String> permissions;
}
