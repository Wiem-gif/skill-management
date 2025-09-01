package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class InvalidHierarchicalHeadException extends ApiException {

    public InvalidHierarchicalHeadException(String message) {
        super(ErrorCodeEnum.SMGT_EMPLOYEE_INVALID_HIERARCHICAL_HEAD, message);
    }

    public InvalidHierarchicalHeadException(ErrorCodeEnum code, String message) {
        super(code, message);
    }
}
