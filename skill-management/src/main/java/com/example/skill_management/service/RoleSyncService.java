package com.example.skill_management.service;

import com.example.skill_management.repository.PermissionRepository;
import com.example.skill_management.repository.RolePermissionRepository;
import com.example.skill_management.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleSyncService implements ApplicationListener<ApplicationReadyEvent> {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Starting role-permission synchronization on application startup...");
        syncRoles()
                .doOnSuccess(unused -> log.info("Role-permission synchronization completed successfully."))
                .doOnError(error -> log.error("Error during role-permission synchronization", error))
                .subscribe();
    }

    public Mono<Void> syncRoles() {
        return reactor.core.publisher.Flux.fromArray(com.example.skill_management.Enum.Role.values())
                .flatMap(enumRole ->
                        roleRepository.findByName(enumRole.name())
                                .switchIfEmpty(roleRepository.save(com.example.skill_management.model.RoleEntity.builder()
                                        .name(enumRole.name())
                                        .build()))
                                .flatMap(roleEntity ->
                                        reactor.core.publisher.Flux.fromIterable(enumRole.getPermissions())
                                                .flatMap(enumPermission ->
                                                        permissionRepository.findByName(enumPermission.getPermission())
                                                                .flatMap(permissionEntity ->
                                                                        rolePermissionRepository
                                                                                .findByRoleIdAndPermissionId(roleEntity.getId(), permissionEntity.getId())
                                                                                .switchIfEmpty(
                                                                                        rolePermissionRepository.save(com.example.skill_management.model.RolePermission.builder()
                                                                                                        .roleId(roleEntity.getId())
                                                                                                        .permissionId(permissionEntity.getId())
                                                                                                        .build())
                                                                                                .doOnNext(saved -> log.info("Inserted RolePermission: {}", saved))
                                                                                )
                                                                )
                                                )
                                                .then()
                                )
                )
                .then();
    }
}
