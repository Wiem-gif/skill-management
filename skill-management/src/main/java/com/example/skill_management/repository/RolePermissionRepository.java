package com.example.skill_management.repository;

import com.example.skill_management.model.RolePermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RolePermissionRepository extends ReactiveCrudRepository<RolePermission, Long> {

    @Query("SELECT * FROM role_permission WHERE role_id = :roleId AND permission_id = :permissionId")
    Mono<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);
    // 🔹 Retourne toutes les relations pour un role donné
    Flux<RolePermission> findByRoleId(Long roleId);

}
