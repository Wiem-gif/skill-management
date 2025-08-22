package com.example.skill_management.demo;


import com.example.skill_management.dto.*;
import com.example.skill_management.model.EmployeeSkill;
import com.example.skill_management.service.EmployeeSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/employee-skills")
@RequiredArgsConstructor
@Tag(name = "Employee Skill Management", description = "API to manage employee skills")
public class EmployeeSkillController {

    private final EmployeeSkillService service;


    @GetMapping
    public Flux<EmployeeSkill> getAll() {
        return service.findAll();
    }

    // Ancienne API commentée : recherche par matricule
    /*
    @GetMapping("/employee/matricule/{matricule}")
    public Flux<EmployeeSkill> getByEmployeeMatricule(@PathVariable String matricule) {
        return service.findByEmployeeMatricule(matricule);
    }
    */

//    @GetMapping("/employee/{employeeId}")
//    public Flux<EmployeeSkill> getByEmployeeId(@PathVariable Long employeeId) {
//        return service.findByEmployeeId(employeeId);
//    }
@GetMapping("/employee/{employeeId}")
public Mono<ResponseEntity<Object>> getEmployeeSkillsDetailed(@PathVariable Long employeeId) {
    return service.getSkillsByEmployeeIdDetailed(employeeId);
}




    //    @GetMapping("/skill/{skillId}")
//    public Flux<EmployeeSkill> getBySkillId(@PathVariable Long skillId) {
//        return service.findBySkillId(skillId);
//    }
@GetMapping("/skill/{skillId}")
public Mono<ResponseEntity<?>> getBySkillId(@PathVariable Long skillId) {
    Flux<EmployeeSkill> employees = service.findBySkillId(skillId);

    return employees.hasElements()
            .flatMap(hasElements -> {
                if (hasElements) {
                    return Mono.just(ResponseEntity.ok(employees));
                } else {
                    return Mono.just(
                            ResponseEntity.status(404)
                                    .body(Map.of("error", "No employees found for this skill"))
                    );
                }
            });
}

    @PostMapping
    public Mono<List<EmployeeSkill>> create(@RequestBody EmployeeSkillsRequest request) {
        return service.createMultiple(request.getEmployeeId(), request.getSkills())
                .collectList();
    }

    // Ancienne API update par matricule et skillId commentée
    /*
    @PutMapping("/matricule/{matricule}/skill/{skillId}")
    public Mono<EmployeeSkill> updateByMatriculeAndSkillId(@PathVariable String matricule,
                                                           @PathVariable Long skillId,
                                                           @RequestBody EmployeeSkill updated) {
        return service.updateByMatriculeAndSkillId(matricule, skillId, updated);
    }
    */

    // Nouvelle API update multiple compétences par employeeId
    @Operation(summary = "Update employee skills by employee ID (Admin only)",
            description = "Restricted to Admin")
    @PutMapping("/employee/{employeeId}")
    public Mono<ResponseEntity<Object>> updateSkills(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeSkillsRequest.SkillLevelDTO> skills) {

        return service.updateSkillsSummary(employeeId, skills)
                .map(summary -> ResponseEntity.ok((Object) summary))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Employee not found")) {
                        return Mono.just(ResponseEntity.status(404)
                                .body(Map.of("error", "Employee with id " + employeeId + " not found")));
                    }
                    return Mono.just(ResponseEntity.status(500)
                            .body(Map.of(
                                    "error", "Internal Server Error",
                                    "details", e.getMessage()
                            )));
                });
    }


    // Ancienne API delete par matricule commentée
    /*
    @DeleteMapping("/matricule/{matricule}")
    public Mono<Void> deleteByMatricule(@PathVariable String matricule) {
        return service.deleteByEmployeeMatricule(matricule);
    }
    */

    // Nouvelle API delete par employeeId (toutes compétences supprimées)
    @DeleteMapping("/{employeeId}")
    public Mono<ResponseEntity<Object>> deleteByEmployeeId(@PathVariable Long employeeId) {
        return service.deleteByEmployeeId(employeeId)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Employee not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body(Map.of("error", "Employee with id " + employeeId + " not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }


//    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public Mono<ResponseEntity<Map<String, Object>>> importEmployeeSkills(
//            @Parameter(
//                    description = "Excel file containing skills per employee",
//                    required = true,
//                    schema = @Schema(type = "string", format = "binary"))
//            @RequestPart("file") FilePart file) {
//        return service.importSkill(file);
//    }

//
@PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
public Mono<ResponseEntity<?>> searchEmployees(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "100") int limit,
        @RequestBody EmployeeSearchRequest request) {

    if ((request.getSkillfilters() == null || request.getSkillfilters().isEmpty()) &&
            (request.getEmployeefilters() == null || request.getEmployeefilters().isEmpty())) {
        return Mono.just(ResponseEntity.badRequest()
                .body("Please provide at least one skill or employee filter"));
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
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
                }
                return Mono.just(ResponseEntity.ok(employees));
            })
            .onErrorResume(e -> {
                log.error("Error searching employees", e);
                Map<String, String> errorBody = Map.of(
                        "message", "An error occurred while searching for employees"
                );
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody));
            });

}




    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
