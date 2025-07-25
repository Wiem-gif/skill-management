package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super(ErrorCodeEnum.SMGT_USER_NOT_FOUND);
    }
}
