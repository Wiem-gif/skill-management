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
    DELETE_EMPLOYEE("delete_employee"),
    CREATE_CATEGORY("create_category"),
    DELETE_GRADE("delete_grade"),
    IMPORT_EMPLOYEE("import_employee"),
    IMPORT_EMPLOYEE_SKILL("import_employee_skill"),
    WRITE_SKILL("write_skill"),
    READ_SKILL("read_skill"),
    DELETE_SKILL("delete_skill"),
    UPDATE_SKILL("update_skill"),
    UPDATE_EMPLOYEE_SKILL("update_employee_skill"),
    DELETE_EMPLOYEE_SKILL("delete_employee_skill"),
    READ_EMPLOYEE_SKILL("read_employee_skill"),
    READ_GRADE("read_grade"),
    UPDATE_GRADE("update_grade"),
    WRITE_GRADE("write_grade"),
    READ_JOB_TITLE("read_jobTitle"),
    READ_EMPLOYEE("read_employee"),
    UPDATE_SKILL_CATEGORY("update_skill_category"),
    DELETE_SKILL_CATEGORY("delete_skill_category"),
    READ_SKILL_CATEGORY("read_skill_category"),
    WRITE_JOB_TITLE("write_jobTitle");

    @Getter
    private final String permission;
}
