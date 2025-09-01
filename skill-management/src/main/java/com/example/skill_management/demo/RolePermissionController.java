package com.example.skill_management.demo;

import com.example.skill_management.dto.AssignPermissionsRequest;
import com.example.skill_management.dto.PermissionRequest;
import com.example.skill_management.dto.RoleRequest;
import com.example.skill_management.model.PermissionEntity;
import com.example.skill_management.model.RoleEntity;
import com.example.skill_management.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role-permission")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles and Permissions  Management")
public class RolePermissionController {

    private final RolePermissionService service;

    // Créer un rôle
    @PostMapping("/roles")

    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Object>> createRole(@RequestBody RoleRequest request) {
        String name = request.getName();
        if (name == null || name.isBlank()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body((Object) Map.of("message", "Role name is required")));
        }

        return service.createRole(name)
                .map(role -> ResponseEntity.status(HttpStatus.CREATED).body((Object) role))
                .onErrorResume(e -> {
                    log.error("Error creating role", e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body((Object) Map.of("message", e.getMessage())));
                });
    }

    // Créer une permission
    @PostMapping("/permissions")


    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Object>> createPermission(@RequestBody PermissionRequest request) {
        String name = request.getName();
        if (name == null || name.isBlank()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body((Object) Map.of("message", "Permission name is required")));
        }

        return service.createPermission(name)
                .map(permission -> ResponseEntity.status(HttpStatus.CREATED).body((Object) permission))
                .onErrorResume(e -> {
                    log.error("Error creating permission", e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body((Object) Map.of("message", e.getMessage())));
                });
    }



    //  Assigner des permissions à un rôle
    @PostMapping("/roles/assign-permissions")

    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Object>> assignPermissions(@RequestBody AssignPermissionsRequest request) {
        String roleName = request.getRole();
        List<String> permissions = request.getPermissions();

        if (roleName == null || permissions == null || permissions.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body((Object) Map.of("message", "Role name and permissions are required")));
        }

        return service.assignPermissions(roleName, permissions)
                .map(msg -> ResponseEntity.ok((Object) Map.of("message", msg)))
                .onErrorResume(e -> {
                    log.error("Error assigning permissions", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body((Object) Map.of("message", e.getMessage())));
                });
    }


    //  Liste des rôles avec permissions
    @GetMapping("/list")
    public Flux<Map<String, Object>> getAllRolesWithPermissions() {
        return service.getAllRolesWithPermissions();
    }
}
