package com.example.skill_management.service;

import com.example.skill_management.model.PermissionEntity;
import com.example.skill_management.model.RoleEntity;
import com.example.skill_management.model.RolePermission;
import com.example.skill_management.repository.PermissionRepository;
import com.example.skill_management.repository.RolePermissionRepository;
import com.example.skill_management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public Mono<RoleEntity> createRole(String name) {
        return roleRepository.findByName(name)
                .switchIfEmpty(roleRepository.save(RoleEntity.builder().name(name).build()));
    }

    public Mono<PermissionEntity> createPermission(String name) {
        return permissionRepository.findByName(name)
                .switchIfEmpty(permissionRepository.save(PermissionEntity.builder().name(name).build()));
    }

    public Mono<String> assignPermissions(String roleName, List<String> permissions) {
        return roleRepository.findByName(roleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found: " + roleName)))
                .flatMap(role ->
                        Flux.fromIterable(permissions)
                                .flatMap(permissionName ->
                                        permissionRepository.findByName(permissionName)
                                                .switchIfEmpty(Mono.error(new RuntimeException("Permission not found: " + permissionName)))
                                                .flatMap(permission ->
                                                        rolePermissionRepository.findByRoleIdAndPermissionId(role.getId(), permission.getId())
                                                                .switchIfEmpty(rolePermissionRepository.save(
                                                                        RolePermission.builder()
                                                                                .roleId(role.getId())
                                                                                .permissionId(permission.getId())
                                                                                .build()
                                                                ))
                                                )
                                )
                                .then(Mono.just("Permissions assigned successfully"))
                );
    }

    public Flux<Map<String, Object>> getAllRolesWithPermissions() {
        return roleRepository.findAll()
                .flatMap(role ->
                        rolePermissionRepository.findByRoleId(role.getId())
                                .flatMap(rp -> permissionRepository.findById(rp.getPermissionId()))
                                .map(PermissionEntity::getName)
                                .collectList()
                                .map(perms -> {
                                    Map<String, Object> map = new LinkedHashMap<>();
                                    map.put("role", role.getName());
                                    map.put("permissions", perms);

                                    return map;
                                })
                );
    }

}
