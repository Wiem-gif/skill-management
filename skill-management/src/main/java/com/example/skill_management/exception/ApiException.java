package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;
import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final String code;
    private final int httpStatus;

    protected ApiException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.code = errorCodeEnum.getCode();
        this.httpStatus = errorCodeEnum.getHttpStatus().value();
    }
}
