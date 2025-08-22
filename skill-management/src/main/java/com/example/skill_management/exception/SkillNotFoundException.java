package com.example.skill_management.exception;

import com.example.skill_management.Enum.ErrorCodeEnum;

public class SkillNotFoundException extends ApiException {
    public SkillNotFoundException() {
        super(ErrorCodeEnum.SMGT_SKILL_NOT_FOUND, "Skill not found");
    }
}
