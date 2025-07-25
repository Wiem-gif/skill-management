package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class RequiredFieldException extends ApiException {
    public RequiredFieldException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum);
    }
}
