package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException() {
        super(ErrorCodeEnum.SMGT_USER_CREATE_EMAIL_ALREADY_EXISTS);
    }
}
