package com.example.skill_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private String firstname;
    private String lastname;
    private String email;
    private String role;
}
