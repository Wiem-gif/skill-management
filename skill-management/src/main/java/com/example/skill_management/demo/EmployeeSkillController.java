package com.example.skill_management.demo;


import com.example.skill_management.dto.*;
import com.example.skill_management.service.EmployeeSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/employee-skills")
@RequiredArgsConstructor
@Tag(name = "Employee Skill Management", description = "API to manage employee skills")
public class EmployeeSkillController {

    private final EmployeeSkillService service;


    @GetMapping("/list")
    @PreAuthorize("hasAuthority('read_employee_skill')")
    public Mono<ResponseEntity<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return service.countAllEmployeeSkills()
                .flatMap(total -> service.findAllWithSkillName()
                        .skip(offset)
                        .take(limit)
                        .collectList()
                        .map(data -> {
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("total", total);
                            response.put("offset", offset);
                            response.put("limit", limit);
                            response.put("data", data);
                            return ResponseEntity.ok(response);
                        })
                )
                .onErrorResume(ex -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("code", "SMGT-0000");
                    error.put("message", ex.getMessage());
                    error.put("status", 500);
                    return Mono.just(ResponseEntity.status(500).body(error));
                });
    }





    @GetMapping("/employee/{employeeId}")
@PreAuthorize("hasAuthority('read_employee_skill')")
public Mono<ResponseEntity<Object>> getEmployeeSkillsDetailed(@PathVariable Long employeeId) {
    return service.getSkillsByEmployeeIdDetailed(employeeId);
}

    @GetMapping("/employee/matricule/{matricule}")
    @PreAuthorize("hasAuthority('read_employee_skill')")
    public Mono<ResponseEntity<Object>> getEmployeeSkillsDetailedByMatricule(@PathVariable String matricule) {
        return service.getSkillsByEmployeeMatriculeDetailed(matricule);
    }






    // Nouvelle API update multiple compétences par employeeId
    @Operation(summary = "Update employee skills by employee ID (Admin only)",
            description = "Restricted to Admin")
    @PutMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('update_employee_skill')")
    public Mono<ResponseEntity<Object>> updateSkills(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeSkillsRequest.SkillLevelDTO> skills) {

        return service.updateSkillsSummary(employeeId, skills)
                .map(summary -> ResponseEntity.ok((Object) summary))
                .onErrorResume(e -> {
                    // 🔹 Gérer l'erreur si l'employé n'existe pas
                    if (e.getMessage().contains("Employee not found")) {
                        return Mono.just(ResponseEntity.status(404)
                                .body(Map.of(
                                        "code", "SMGT-0404",
                                        "message", "Employee with id " + employeeId + " not found",
                                        "status", 404
                                )));
                    }
                    // 🔹 Gérer accès refusé
                    if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
                        return Mono.just(ResponseEntity.status(403)
                                .body(Map.of(
                                        "code", "SMGT-0403",
                                        "message", "Forbidden - Access Denied",
                                        "status", 403
                                )));
                    }
                    // 🔹 Autres erreurs serveur → 500
                    return Mono.just(ResponseEntity.status(500)
                            .body(Map.of(
                                    "code", "SMGT-0000",
                                    "message", e.getMessage(),
                                    "status", 500
                            )));
                });

    }



    // Nouvelle API delete par employeeId (toutes compétences supprimées)
    @DeleteMapping("/{employeeId}")
    @Operation(summary = "Restricted to Admin")
    @PreAuthorize("hasAuthority('delete_employee_skill')")
    public Mono<ResponseEntity<Map<String, Object>>> deleteByEmployeeId(@PathVariable Long employeeId) {
        return service.deleteByEmployeeId(employeeId)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = Map.of(
                            "status", "success",
                            "message", "Employee skills deleted successfully"
                    );
                    return ResponseEntity.ok(response); // HTTP 200 avec JSON
                }))
                .onErrorResume(e -> {
                    if (e instanceof org.springframework.security.access.AccessDeniedException) {
                        return Mono.just(
                                ResponseEntity.status(403)
                                        .body(Map.of(
                                                "code", "SMGT-0403",
                                                "message", "Forbidden - Access Denied",
                                                "status", 403
                                        ))
                        );
                    }
                    if (e.getMessage() != null && e.getMessage().contains("Employee not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Employee with id " + employeeId + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }




    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> searchEmployees(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit,
            @RequestBody EmployeeSearchRequest request) {

        if ((request.getSkillfilters() == null || request.getSkillfilters().isEmpty()) &&
                (request.getEmployeefilters() == null || request.getEmployeefilters().isEmpty())) {
            return Mono.just(ResponseEntity.badRequest()
                    .body((Object) "Please provide at least one skill or employee filter"));
        }

        return service.searchEmployees(
                        request.getSkillfilters(),
                        request.getEmployeefilters(),
                        offset,
                        limit)
                .collectList()
                .flatMap(employees -> {
                    if (employees.isEmpty()) {
                        Map<String, String> body = Map.of(
                                "message", "No employees found matching the filters"
                        );
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body((Object) body));
                    }

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("total", employees.size());
                    response.put("offset", offset);
                    response.put("limit", limit);
                    response.put("data", employees);

                    return Mono.just(ResponseEntity.ok((Object) response));
                })
                .onErrorResume(e -> {
                    log.error("Error searching employees", e);
                    Map<String, String> errorBody = Map.of(
                            "message", "An error occurred while searching for employees"
                    );
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body((Object) errorBody));
                });
    }





    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Restricted to Admin")
    @PreAuthorize("hasAuthority('import_employee_skill')")
    public Mono<ResponseEntity<EmployeeSkillImportResponse>> importEmployeeSkills(
            @Parameter(
                    description = "Excel file containing skills per employee",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            format = "binary",
                            extensions = {
                                    @io.swagger.v3.oas.annotations.extensions.Extension(
                                            name = "x-accept",
                                            properties = {
                                                    @io.swagger.v3.oas.annotations.extensions.ExtensionProperty(name = "value", value = ".xlsx,.xls")
                                            }
                                    )
                            }
                    )
            )
            @RequestPart("file") FilePart file) {


        return service.importSkill(file);
    }



}
