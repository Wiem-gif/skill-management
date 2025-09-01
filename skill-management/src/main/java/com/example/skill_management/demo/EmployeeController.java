package com.example.skill_management.demo;

import com.example.skill_management.dto.EmployeeImportResult;
import com.example.skill_management.dto.EmployeeUpdateRequest;
import com.example.skill_management.exception.EmployeeNotFoundException;
import com.example.skill_management.model.Employee;
import com.example.skill_management.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("/skill-management/employee")
@RequiredArgsConstructor
@Tag(name = "Employee  Management", description = "API to manage employee ")

public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('write_employee')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Map<String, Object>>> createEmployee(@RequestBody Employee employee) {
        return employeeService.createEmployee(employee)
                .map(saved -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Employee created successfully");
                    response.put("data", saved);
                    return ResponseEntity.ok(response);
                });

    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('import_employee')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Map<String, Object>>> importEmployees(
            @Parameter(description = "File to upload",
                    required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") FilePart file) {

        return employeeService.importEmployees(file)
                .collectList()
                .map(results -> {
                    List<Map<String, Object>> saved = new ArrayList<>();
                    List<Map<String, Object>> errors = new ArrayList<>();

                    for (EmployeeImportResult r : results) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("row", r.getRowNumber());
                        item.put("employeeMatricule", r.getEmployeeMatricule());

                        if (r.getError() == null) {
                            saved.add(item);
                        } else {
                            item.put("message", r.getError());
                            errors.add(item);
                        }
                    }


                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("status", "success");
                    response.put("message", "Employees import completed");
                    response.put("totalRows", results.size());
                    response.put("successCount", saved.size());
                    response.put("errorCount", errors.size());
                    response.put("success", saved);
                    response.put("errors", errors);

                    return ResponseEntity.ok(response);
                });
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_employee')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Map<String, Object>>> deleteEmployee(@PathVariable Long id) {
        return employeeService.deleteEmployee(id)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("status", "success");
                    response.put("message", "Employee and related skills deleted successfully");
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(EmployeeNotFoundException.class, e -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("status", "error");
                    error.put("code", e.getCode());
                    error.put("message", "Employee not found");
                    return Mono.just(ResponseEntity.status(e.getHttpStatus()).body(error));
                });
    }

    // 🔹 Mettre à jour un employé
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_employee')")
    @Operation(summary = "Restricted to Admin")
    public Mono<ResponseEntity<Map<String, Object>>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeUpdateRequest request) {

        return employeeService.updateEmployee(id, request)
                .then(Mono.fromSupplier(() -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("status", "success");
                    response.put("message", "Employee updated successfully");
                    return ResponseEntity.ok(response);
                }))
                .onErrorResume(EmployeeNotFoundException.class, e -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("status", "error");
                    error.put("code", e.getCode());
                    error.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.status(404).body(error));
                });
    }



    @GetMapping("/list")
    public Mono<ResponseEntity<Map<String, Object>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        return employeeService.countAllEmployees()
                .flatMap(total -> employeeService.findAll()
                        .skip(offset)
                        .take(limit)
                        .collectList()
                        .map(employees -> {
                            Map<String, Object> response = new LinkedHashMap<>();
                            response.put("total", total);
                            response.put("offset", offset);
                            response.put("limit", limit);
                            response.put("data", employees);
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



    @GetMapping("/search/by-matricule")
    public Mono<ResponseEntity<Object>> getEmployeeByMatricule(@RequestParam String matricule) {
        return employeeService.findByMatricule(matricule)
                .map(employee -> ResponseEntity.<Object>ok(employee))
                .onErrorResume(e -> {
                    Map<String, Object> error = new LinkedHashMap<>();
                    if (e instanceof EmployeeNotFoundException ex) {
                        error.put("status", "error");
                        error.put("code", ex.getCode());
                        error.put("message", ex.getMessage());
                        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(error));
                    }
                    error.put("status", "error");
                    error.put("message", "Unexpected error");
                    return Mono.just(ResponseEntity.status(500).body(error));
                });
    }





}
