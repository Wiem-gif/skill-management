package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class EmployeeAlreadyExistsException extends ApiException {
    public EmployeeAlreadyExistsException(ErrorCodeEnum errorCode) {
        super(errorCode);
    }
}