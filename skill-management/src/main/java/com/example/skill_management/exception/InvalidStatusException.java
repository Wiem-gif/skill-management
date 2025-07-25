package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class InvalidStatusException extends ApiException {
    public InvalidStatusException(ErrorCodeEnum smgtEmployeeInvalidStatus) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_INVALID_STATUS);
    }
}