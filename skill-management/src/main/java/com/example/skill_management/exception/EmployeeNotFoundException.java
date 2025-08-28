package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class EmployeeNotFoundException extends ApiException {

    public EmployeeNotFoundException(String firstname, String lastname) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_NOT_FOUND,
                "Employee not found with firstname: " + firstname + " and lastname: " + lastname);
    }
    public EmployeeNotFoundException(Long id) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_NOT_FOUND,
                "Employee not found with id: " + id);
    }
    public EmployeeNotFoundException(String matricule) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_NOT_FOUND,
                "Employee not found with matricule: " + matricule);
    }
}
