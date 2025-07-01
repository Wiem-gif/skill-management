package com.example.skill_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String firstname,
        @NotBlank String lastname,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String role
) {}