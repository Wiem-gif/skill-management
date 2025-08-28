package com.example.skill_management.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {

    USER(Set.of(
            Permission.READ_USER,
            Permission.READ_EMPLOYEE_SKILL,
            Permission.READ_GRADE,
            Permission.READ_JOB_TITLE,
            Permission.READ_SKILL,
            Permission.READ_SKILL_CATEGORY
    )),

    ADMIN(Set.of(
            Permission.WRITE_USER,
            Permission.DELETE_USER,
            Permission.READ_USER,
            Permission.WRITE_EMPLOYEE,
            Permission.UPDATE_EMPLOYEE,
            Permission.DELETE_EMPLOYEE,
            Permission.CREATE_CATEGORY,
            Permission.DELETE_GRADE,
            Permission.IMPORT_EMPLOYEE,
            Permission.IMPORT_EMPLOYEE_SKILL,
            Permission.DELETE_EMPLOYEE_SKILL,
            Permission.UPDATE_EMPLOYEE_SKILL,
            Permission.READ_EMPLOYEE_SKILL,
            Permission.UPDATE_SKILL,
            Permission.READ_SKILL,
            Permission.WRITE_SKILL,
            Permission.DELETE_SKILL,
            Permission.UPDATE_GRADE,
            Permission.WRITE_GRADE,
            Permission.READ_GRADE,
            Permission.READ_JOB_TITLE,
            Permission.WRITE_JOB_TITLE,
            Permission.READ_EMPLOYEE,

            Permission.DELETE_SKILL_CATEGORY,
            Permission.READ_SKILL_CATEGORY
    )),

    TechnicalManager(Set.of(
            Permission.READ_USER,
            Permission.READ_EMPLOYEE,
            Permission.READ_EMPLOYEE_SKILL,
            Permission.READ_GRADE,
            Permission.READ_JOB_TITLE,
            Permission.READ_SKILL,
            Permission.READ_SKILL_CATEGORY

    )),

    QualityManager(Set.of(
            Permission.READ_USER,
            Permission.READ_EMPLOYEE,
            Permission.READ_EMPLOYEE_SKILL,
            Permission.READ_GRADE,
            Permission.READ_JOB_TITLE,
            Permission.READ_SKILL,
            Permission.READ_SKILL_CATEGORY

    ));

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
