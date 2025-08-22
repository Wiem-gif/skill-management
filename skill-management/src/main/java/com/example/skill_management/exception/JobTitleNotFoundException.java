package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class JobTitleNotFoundException extends ApiException {

    public JobTitleNotFoundException(String jobTitleName) {
        super(ErrorCodeEnum.SMGT_JOBTITLE_NOT_FOUND, "Job title not found: " + jobTitleName);
    }


}