package com.example.skill_management.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    WRITE_USER("write_user"),
    DELETE_USER("delete_user"),
    READ_USER("read_user"),
    WRITE_EMPLOYEE("write_employee"),
    UPDATE_EMPLOYEE("update_employee"),
    DELETE_EMPLOYEE("delete_employee");

    @Getter
    private final String permission;
}
