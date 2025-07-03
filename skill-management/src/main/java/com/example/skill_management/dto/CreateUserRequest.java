package com.example.skill_management.dto;

import com.example.skill_management.Enum.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
}
