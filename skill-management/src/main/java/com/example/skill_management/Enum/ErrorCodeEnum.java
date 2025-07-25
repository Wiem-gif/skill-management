package com.example.skill_management.Enum;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeEnum {

    SMGT_USER_CREATE_REQUIRED_FIRSTNAME("SMGT-0001", "Firstname is required", HttpStatus.BAD_REQUEST),
    SMGT_USER_CREATE_REQUIRED_LASTNAME("SMGT-0002", "Lastname is required", HttpStatus.BAD_REQUEST),
    SMGT_USER_CREATE_REQUIRED_EMAIL("SMGT-0003", "Email is required", HttpStatus.BAD_REQUEST),
    SMGT_USER_CREATE_REQUIRED_PASSWORD("SMGT-0004", "Password is required", HttpStatus.BAD_REQUEST),
    SMGT_USER_CREATE_REQUIRED_ROLE("SMGT-0005", "RoleEntity is required", HttpStatus.BAD_REQUEST),
    SMGT_USER_CREATE_EMAIL_ALREADY_EXISTS("SMGT-0006", "Email already in use", HttpStatus.BAD_REQUEST),
    SMGT_USER_DELETE_PROTECTED("SMGT-0007", "Cannot delete a protected user", HttpStatus.BAD_REQUEST),
    SMGT_USER_NOT_FOUND("SMGT-0008", "User not found", HttpStatus.NOT_FOUND),

    SMGT_EMPLOYEE_REQUIRED_MATRICULE("SMGT-0009", "Employee matricule is required", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_REQUIRED_FIRSTNAME("SMGT-0010", "First name is required", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_REQUIRED_LASTNAME("SMGT-0011", "Last name is required", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_REQUIRED_EMAIL("SMGT-0012", "Email is required", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_REQUIRED_CIN("SMGT-0013", "National ID is required", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_REQUIRED_DATE_ENTRY("SMGT-0014", "Start date is required", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_REQUIRED_CONTRACT_TYPE("SMGT-0015", "Contract type is required", HttpStatus.BAD_REQUEST),

    // Uniqueness violations
    SMGT_EMPLOYEE_DUPLICATE_MATRICULE("SMGT-0016", "Employee matricule already exists", HttpStatus.CONFLICT),
    SMGT_EMPLOYEE_DUPLICATE_EMAIL("SMGT-0017", "Email already exists", HttpStatus.CONFLICT),
    SMGT_EMPLOYEE_DUPLICATE_CIN("SMGT-0018", "National ID already exists", HttpStatus.CONFLICT),

    // Format/Validation errors
    SMGT_EMPLOYEE_INVALID_EMAIL("SMGT-0019", "Invalid email format", HttpStatus.BAD_REQUEST),
    SMGT_EMPLOYEE_INVALID_STATUS("SMGT-0020", "Invalid status (allowed values: ACTIVE, INACTIVE, ON_LEAVE)", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeEnum(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
