package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class MissingEmployeeFieldException extends ApiException {

    public MissingEmployeeFieldException(ErrorCodeEnum errorCode) {
        super(errorCode);
        validateErrorCode(errorCode);
    }

    private void validateErrorCode(ErrorCodeEnum errorCode) {
        if (!errorCode.name().startsWith("SMGT_EMPLOYEE_REQUIRED_")) {
            throw new IllegalArgumentException("Invalid error code for missing field");
        }
    }
}