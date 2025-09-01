package com.example.skill_management.service;

import com.example.skill_management.dto.EmployeeImportResult;

import com.example.skill_management.dto.EmployeeUpdateRequest;
import com.example.skill_management.exception.*;
import com.example.skill_management.model.Employee;
import com.example.skill_management.Enum.ErrorCodeEnum;
import com.example.skill_management.repository.EmployeeRepository;
import com.example.skill_management.repository.EmployeeSkillRepository;
import com.example.skill_management.repository.GradeRepository;
import com.example.skill_management.repository.JobTitleRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final JobTitleRepository jobTitleRepository;
    private final GradeRepository gradeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    public Mono<Employee> createEmployee(Employee employee) {
        validateEmployee(employee);
        Employee employeeToSave = buildEmployeeToSave(employee);

        return checkDuplicate(employeeToSave)
                .then(employeeRepository.save(employeeToSave));
    }

    private Employee buildEmployeeToSave(Employee employee) {
        return Employee.builder()
                .matricule(employee.getMatricule())
                .firstname(employee.getFirstname())
                .lastname(employee.getLastname())
                .gender(employee.getGender())
                .birthday(employee.getBirthday())
                .cin(employee.getCin())
                .email(employee.getEmail())
                .activity(employee.getActivity())
                .gradeId(employee.getGradeId())
                .function(employee.getFunction())
                .previousExperience(employee.getPreviousExperience())
                .hierarchicalHead(employee.getHierarchicalHead())
                .dateEntry(employee.getDateEntry())
                .contractType(employee.getContractType())
                .contractEnd(employee.getContractEnd())
                .status(employee.getStatus())
                .jobTitleId(employee.getJobTitleId())
                .build();
    }

    private void validateEmployee(Employee employee) {
        if (isNullOrEmpty(employee.getMatricule())) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_MATRICULE);
        }
        if (isNullOrEmpty(employee.getFirstname())) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_FIRSTNAME);
        }
        if (isNullOrEmpty(employee.getLastname())) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_LASTNAME);
        }
        if (isNullOrEmpty(employee.getEmail())) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_EMAIL);
        }
        if (isNullOrEmpty(employee.getCin())) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_CIN);
        }
        if (employee.getDateEntry() == null) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_DATE_ENTRY);
        }
        if (isNullOrEmpty(employee.getContractType())) {
            throw new MissingEmployeeFieldException(ErrorCodeEnum.SMGT_EMPLOYEE_REQUIRED_CONTRACT_TYPE);
        }

        if (!employee.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidEmailException(ErrorCodeEnum.SMGT_EMPLOYEE_INVALID_EMAIL);
        }

        if (employee.getStatus() != null &&
                !List.of("ACTIVE", "INACTIVE", "ON_LEAVE").contains(employee.getStatus())) {
            throw new InvalidStatusException(ErrorCodeEnum.SMGT_EMPLOYEE_INVALID_STATUS);
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Mono<Void> checkDuplicate(Employee employee) {
        return employeeRepository.findByMatriculeIgnoreCase(employee.getMatricule())
                .flatMap(existing -> Mono.error(new DuplicateMatriculeException(ErrorCodeEnum.SMGT_EMPLOYEE_DUPLICATE_MATRICULE)))
                .switchIfEmpty(employeeRepository.findByEmail(employee.getEmail())
                        .flatMap(existing -> Mono.error(new DuplicateEmailException(ErrorCodeEnum.SMGT_EMPLOYEE_DUPLICATE_EMAIL))))
                .switchIfEmpty(employeeRepository.findByCin(employee.getCin())
                        .flatMap(existing -> Mono.error(new DuplicateCinException(ErrorCodeEnum.SMGT_EMPLOYEE_DUPLICATE_CIN))))
                .then();
    }

    public Flux<EmployeeImportResult> importEmployees(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .flatMapMany(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return parseWorkbookReactive(bytes);
                })
                .collectList() // On récupère tous les employés pour construire le set des noms
                .flatMapMany(employees -> {
                    Set<String> employeeFullNamesInFile = employees.stream()
                            .map(e -> e.getFirstname() + " " + e.getLastname())
                            .collect(Collectors.toSet());

                    return Flux.fromIterable(employees)
                            .index()
                            .concatMap(tuple -> {
                                long rowIndex = tuple.getT1() + 2;
                                Employee employee = tuple.getT2();
                                return createEmployeeForImport(employee, (int) rowIndex, employeeFullNamesInFile);
                            });
                });
    }



    private Flux<Employee> parseWorkbookReactive(byte[] bytes) {
        return Mono.fromCallable(() -> {
                    List<Employee> employees = new ArrayList<>();
                    try (InputStream inputStream = new ByteArrayInputStream(bytes);
                         Workbook workbook = WorkbookFactory.create(inputStream)) {

                        Sheet sheet = workbook.getSheetAt(0);
                        int lastRowNum = sheet.getLastRowNum();

                        for (int i = 1; i <= lastRowNum; i++) {
                            Row row = sheet.getRow(i);
                            if (row == null || isRowEmpty(row)) continue;

                            String jobTitleName = getStringCellValue(row.getCell(16));
                            String gradeCode = getStringCellValue(row.getCell(8));

                            Long jobTitleId = jobTitleRepository.findByName(jobTitleName)
                                    .map(jt -> jt.getId())
                                    .block();

                            if (jobTitleId == null) {
                                throw new JobTitleNotFoundException(jobTitleName);
                            }
                            Long gradeId = gradeRepository.findByCode(gradeCode)
                                    .map(g -> g.getId())
                                    .block();

                            if (gradeId == null) {
                                throw new GradeNotFoundException(gradeCode);
                            }

                            Employee employee = mapRowToEmployee(row, jobTitleId, gradeId);
                            employees.add(employee);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse workbook: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to parse workbook", e);
                    }
                    return employees;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getStringCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private Employee mapRowToEmployee(Row row, Long jobTitleId, Long gradeId) {
        return Employee.builder()
                .matricule(getStringCellValue(row.getCell(0)))
                .firstname(getStringCellValue(row.getCell(1)))
                .lastname(getStringCellValue(row.getCell(2)))
                .gender(getStringCellValue(row.getCell(3)))
                .birthday(getDateCellValue(row.getCell(4)))
                .cin(getStringCellValue(row.getCell(5)))
                .email(getStringCellValue(row.getCell(6)))
                .activity(getStringCellValue(row.getCell(7)))
                .gradeId(gradeId)
                .function(getStringCellValue(row.getCell(9)))
                .previousExperience(getNumericCellValue(row.getCell(10)))
                .hierarchicalHead(getStringCellValue(row.getCell(11)))
                .dateEntry(getDateCellValue(row.getCell(12)))
                .contractType(getStringCellValue(row.getCell(13)))
                .contractEnd(getDateCellValue(row.getCell(14)))
                .status(getStringCellValue(row.getCell(15)))
                .jobTitleId(jobTitleId)
                .build();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private LocalDate getDateCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            if (cell.getCellType() == CellType.FORMULA && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception e) {
            log.warn("Error parsing date from cell: {}", e.getMessage());
        }
        return null;
    }

    private Integer getNumericCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (!value.isEmpty()) {
                    return Integer.parseInt(value);
                }
            }
            if (cell.getCellType() == CellType.FORMULA) {
                return (int) cell.getNumericCellValue();
            }
        } catch (NumberFormatException e) {
            log.warn("Error parsing numeric value from cell: {}", e.getMessage());
        }
        return null;
    }

//    public Mono<Employee> findByFirstnameAndLastname(String firstname, String lastname) {
//        return employeeRepository.findByFirstnameAndLastname(firstname, lastname)
//                .switchIfEmpty(Mono.error(
//                        new EmployeeNotFoundException(firstname, lastname)
//                ));
//    }

    public Mono<Void> deleteEmployee(Long id) {
        return employeeRepository.findById(id)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException("EMPLOYEE_NOT_FOUND", "Employee not found")))
                .flatMap(employee ->
                        employeeSkillRepository.deleteByEmployeeId(employee.getId())
                                .then(employeeRepository.delete(employee))
                );
    }

    // 🔹 Mettre à jour un employé
    public Mono<Void> updateEmployee(Long id, EmployeeUpdateRequest request) {
        return employeeRepository.findById(id)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(id)))
                .flatMap(existing -> {
                    if (request.getMatricule() != null) {
                        existing.setMatricule(request.getMatricule());
                    }
                    if (request.getFirstname() != null) {
                        existing.setFirstname(request.getFirstname());
                    }
                    if (request.getLastname() != null) {
                        existing.setLastname(request.getLastname());
                    }
                    if (request.getGender() != null) {
                        existing.setGender(request.getGender());
                    }
                    if (request.getBirthday() != null) {
                        existing.setBirthday(request.getBirthday());
                    }
                    if (request.getCin() != null) {
                        existing.setCin(request.getCin());
                    }
                    if (request.getEmail() != null) {
                        existing.setEmail(request.getEmail());
                    }
                    if (request.getActivity() != null) {
                        existing.setActivity(request.getActivity());
                    }
                    if (request.getGradeId() != null) {
                        existing.setGradeId(request.getGradeId());
                    }
                    if (request.getFunction() != null) {
                        existing.setFunction(request.getFunction());
                    }
                    if (request.getPreviousExperience() != null) {
                        existing.setPreviousExperience(request.getPreviousExperience());
                    }
                    if (request.getHierarchicalHead() != null) {
                        existing.setHierarchicalHead(request.getHierarchicalHead());
                    }
                    if (request.getDateEntry() != null) {
                        existing.setDateEntry(request.getDateEntry());
                    }
                    if (request.getContractType() != null) {
                        existing.setContractType(request.getContractType());
                    }
                    if (request.getContractEnd() != null) {
                        existing.setContractEnd(request.getContractEnd());
                    }
                    if (request.getStatus() != null) {
                        existing.setStatus(request.getStatus());
                    }
                    if (request.getJobTitleId() != null) {
                        existing.setJobTitleId(request.getJobTitleId());
                    }

                    return employeeRepository.save(existing).then();
                });
    }



    public Flux<Employee> findAll() {
        return employeeRepository.findAll();
    }
    public Mono<Long> countAllEmployees() {
        return employeeRepository.count();
    }


    public Mono<Employee> findByMatricule(String matricule) {
        return employeeRepository.findByMatricule(matricule)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(matricule)));

    }

    public Mono<EmployeeImportResult> createEmployeeForImport(Employee employee, int rowIndex, Set<String> employeeFullNamesInFile) {
        validateEmployee(employee);
        Employee employeeToSave = buildEmployeeToSave(employee);

        return checkHierarchicalHead(employeeToSave, employeeFullNamesInFile)
                .then(checkDuplicate(employeeToSave))
                .then(employeeRepository.save(employeeToSave))
                .map(saved -> new EmployeeImportResult(rowIndex, saved.getMatricule(), null))
                .onErrorResume(e -> {
                    String errorMessage;

                    if (e instanceof DuplicateMatriculeException) {
                        errorMessage = "Employee matricule already exists";
                    } else if (e instanceof DuplicateEmailException) {
                        errorMessage = "Email already exists";
                    } else if (e instanceof DuplicateCinException) {
                        errorMessage = "CIN already exists";
                    } else if (e instanceof InvalidHierarchicalHeadException) {
                        errorMessage = e.getMessage();
                    } else if (e.getMessage() != null &&
                            (e.getMessage().contains("uq_employee_matricule") || e.getMessage().contains("employee_matricule_key"))) {
                        errorMessage = "Employee matricule already exists";
                    } else if (e.getMessage() != null &&
                            (e.getMessage().contains("uq_employee_email") || e.getMessage().contains("employee_email_key"))) {
                        errorMessage = "Email already exists";
                    } else if (e.getMessage() != null &&
                            (e.getMessage().contains("uq_employee_cin") || e.getMessage().contains("employee_cin_key"))) {
                        errorMessage = "CIN already exists";
                    } else {
                        errorMessage = e.getMessage();
                    }

                    return Mono.just(new EmployeeImportResult(rowIndex, employee.getMatricule(), errorMessage));
                });
    }
    private Mono<Void> checkHierarchicalHead(Employee employee, Set<String> employeeFullNamesInFile) {
        String head = employee.getHierarchicalHead();

        // Si NULL → OK
        if (head == null || head.trim().isEmpty()) {
            return Mono.empty();
        }

        // Séparer firstname et lastname du chef
        String[] parts = head.split(" ", 2);
        if (parts.length < 2) {
            return Mono.error(new InvalidHierarchicalHeadException(
                    "Hierarchical head '" + head + "' format invalid (must be 'Firstname Lastname')"));
        }

        String headFirstname = parts[0];
        String headLastname = parts[1];

        // Vérifier que l'employé ne se référence pas lui-même
        if (headFirstname.equals(employee.getFirstname()) && headLastname.equals(employee.getLastname())) {
            return Mono.error(new InvalidHierarchicalHeadException(
                    "Employee cannot be their own hierarchical head"));
        }

        String fullHeadName = headFirstname + " " + headLastname;

        // Vérifier d'abord dans le fichier
        if (employeeFullNamesInFile.contains(fullHeadName)) return Mono.empty();

        // Sinon vérifier dans la base
        return employeeRepository.findByFirstnameAndLastname(headFirstname, headLastname)
                .switchIfEmpty(Mono.error(new InvalidHierarchicalHeadException(
                        "Hierarchical head '" + head + "' does not exist")))
                .then();
    }




}
