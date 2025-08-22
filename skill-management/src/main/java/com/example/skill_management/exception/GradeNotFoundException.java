package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class GradeNotFoundException extends ApiException {

    public GradeNotFoundException(String gradeCode) {
        super(ErrorCodeEnum.SMGT_GRADE_NOT_FOUND, "Grade title not found: " + gradeCode);
    }


}