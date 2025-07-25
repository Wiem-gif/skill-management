package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class DuplicateCinException extends ApiException {
    public DuplicateCinException(ErrorCodeEnum smgtEmployeeDuplicateCin) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_DUPLICATE_CIN);
    }
}