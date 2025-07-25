package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class InvalidEmailException extends ApiException {
    public InvalidEmailException(ErrorCodeEnum smgtEmployeeInvalidEmail) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_INVALID_EMAIL);
    }
}