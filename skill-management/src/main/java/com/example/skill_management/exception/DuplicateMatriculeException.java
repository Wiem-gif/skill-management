package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;


public class DuplicateMatriculeException extends ApiException {
    public DuplicateMatriculeException(ErrorCodeEnum smgtEmployeeDuplicateMatricule) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_DUPLICATE_MATRICULE);
    }
}