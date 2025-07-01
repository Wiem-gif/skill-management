package com.example.skill_management.dto;

public record UserResponse(
        Long id,
        String firstname,
        String lastname,
        String email,
        String role,
        boolean status,
        boolean protectedFlag
) {}