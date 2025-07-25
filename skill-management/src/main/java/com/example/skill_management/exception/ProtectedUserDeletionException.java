package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class ProtectedUserDeletionException extends ApiException {
    public ProtectedUserDeletionException() {
        super(ErrorCodeEnum.SMGT_USER_DELETE_PROTECTED);
    }
}
