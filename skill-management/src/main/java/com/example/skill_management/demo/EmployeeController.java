package com.example.skill_management.demo;

import com.example.skill_management.model.Employee;
import com.example.skill_management.service.EmployeeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/skill-management/employee")
@RequiredArgsConstructor

public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('write_employee')")

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
    @PreAuthorize("hasAuthority('write_employee')")

    public Mono<ResponseEntity<Map<String, Object>>> importEmployees(
            @Parameter(description = "File to upload",
                    required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") FilePart file) {

        return employeeService.importEmployees(file)
                .collectList()
                .map(results -> {
                    long successCount = results.stream().filter(r -> r.getError() == null).count();
                    long errorCount = results.size() - successCount;

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Employee import completed");
                    response.put("totalRows", results.size());
                    response.put("successCount", successCount);
                    response.put("errorCount", errorCount);
                    response.put("details", results);

                    return ResponseEntity.ok(response);
                });

    }
}
