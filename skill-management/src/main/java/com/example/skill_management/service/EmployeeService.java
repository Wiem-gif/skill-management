package com.example.skill_management.service;

import com.example.skill_management.dto.EmployeeImportResult;
import com.example.skill_management.exception.*;
import com.example.skill_management.model.Employee;
import com.example.skill_management.Enum.ErrorCodeEnum;
import com.example.skill_management.repository.EmployeeRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

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
                .grade(employee.getGrade())
                .function(employee.getFunction())
                .previousExperience(employee.getPreviousExperience())
                .hierarchicalHead(employee.getHierarchicalHead())
                .dateEntry(employee.getDateEntry())
                .contractType(employee.getContractType())
                .contractEnd(employee.getContractEnd())
                .status(employee.getStatus())
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
        return employeeRepository.findByMatricule(employee.getMatricule())
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

                    return Mono.fromCallable(() -> parseWorkbook(bytes))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMapMany(Flux::fromIterable);
                })
                .index()
                .flatMap(tuple -> {
                    long rowIndex = tuple.getT1() + 2;
                    Employee employee = tuple.getT2();

                    return createEmployee(employee)
                            .map(saved -> new EmployeeImportResult((int) rowIndex, saved, null))
                            .onErrorResume(ApiException.class, e ->
                                    Mono.just(new EmployeeImportResult((int) rowIndex, null, e.getMessage()))
                            );
                });
    }

    private List<Employee> parseWorkbook(byte[] bytes) {
        List<Employee> employees = new ArrayList<>();
        try (InputStream inputStream = new ByteArrayInputStream(bytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    Employee employee = mapRowToEmployee(row);
                    employees.add(employee);
                } catch (Exception e) {
                    log.warn("Skipping row {} due to parse error: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse workbook: {}", e.getMessage(), e);
        }
        return employees;
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

    private Employee mapRowToEmployee(Row row) {
        return Employee.builder()
                .matricule(getStringCellValue(row.getCell(0)))
                .firstname(getStringCellValue(row.getCell(1)))
                .lastname(getStringCellValue(row.getCell(2)))
                .gender(getStringCellValue(row.getCell(3)))
                .birthday(getDateCellValue(row.getCell(4)))
                .cin(getStringCellValue(row.getCell(5)))
                .email(getStringCellValue(row.getCell(6)))
                .activity(getStringCellValue(row.getCell(7)))
                .grade(getStringCellValue(row.getCell(8)))
                .function(getStringCellValue(row.getCell(9)))
                .previousExperience(getNumericCellValue(row.getCell(10)))
                .hierarchicalHead(getStringCellValue(row.getCell(11)))
                .dateEntry(getDateCellValue(row.getCell(12)))
                .contractType(getStringCellValue(row.getCell(13)))
                .contractEnd(getDateCellValue(row.getCell(14)))
                .status(getStringCellValue(row.getCell(15)))
                .build();
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
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
}
