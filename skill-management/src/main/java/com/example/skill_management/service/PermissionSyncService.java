package com.example.skill_management.service;

import com.example.skill_management.Enum.Permission;
import com.example.skill_management.model.PermissionEntity;
import com.example.skill_management.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PermissionSyncService {

    private final PermissionRepository permissionRepository;

    @PostConstruct
    public void syncPermissions() {
        Flux.fromArray(Permission.values())
                .flatMap(enumPermission ->
                        permissionRepository.findByName(enumPermission.getPermission())
                                .switchIfEmpty(permissionRepository.save(
                                        PermissionEntity.builder()
                                                .name(enumPermission.getPermission())
                                                .build()
                                ))
                )
                .subscribe();
    }
}
