package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class EmployeeImportException extends ApiException {
    public EmployeeImportException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }
}