package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class DuplicateEmailException extends ApiException {
    public DuplicateEmailException(ErrorCodeEnum smgtEmployeeDuplicateEmail) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_DUPLICATE_EMAIL);
    }
}