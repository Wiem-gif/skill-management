package com.example.skill_management.service;

import com.example.skill_management.dto.*;
import com.example.skill_management.model.*;

import com.example.skill_management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSkillService {

    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final JobTitleRepository jobTitleRepository;



    private static class TempEmployeeSkill {
        public String employeeMatricule;
        public String skillName;
        public String currentLevel;
        public int row;


        public TempEmployeeSkill(String employeeMatricule, String skillName, String currentLevel, int row) {
            this.employeeMatricule = employeeMatricule;
            this.skillName = skillName;
            this.currentLevel = currentLevel;
            this.row = row;
        }


        public TempEmployeeSkill(String employeeMatricule, String skillName, String currentLevel) {
            this(employeeMatricule, skillName, currentLevel, 0);
        }
    }

    private static class ImportResult {
        public String employeeMatricule;
        public String skillName;
        public String action;          // "Saved", "Updated", "Error"
        public String errorMessage;
        public int row;
        public String category;        // pour le SkillInfo

        public ImportResult(String employeeMatricule, String skillName, String action, String errorMessage, int row, String category) {
            this.employeeMatricule = employeeMatricule;
            this.skillName = skillName;
            this.action = action;
            this.errorMessage = errorMessage;
            this.row = row;
            this.category = category;
        }
    }



    public Flux<EmployeeSkill> findAll() {
        return employeeSkillRepository.findAll();
    }

//    public Flux<EmployeeSkill> findByEmployeeId(Long employeeId) {
//        return employeeSkillRepository.findByEmployeeId(employeeId);
//    }

    public Mono<ResponseEntity<Object>> getSkillsByEmployeeIdDetailed(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found")))
                .flatMap(employee ->
                        employeeSkillRepository.findByEmployeeId(employeeId)
                                .collectList()
                                .flatMap(employeeSkills ->
                                        Flux.fromIterable(employeeSkills)
                                                .flatMap(empSkill -> skillRepository.findById(empSkill.getSkillId())
                                                        .flatMap(skill -> skillCategoryRepository.findById(skill.getSkillCategoryId())
                                                                .map(category -> new EmployeeSkillsResponse.SkillInfo(
                                                                        skill.getId(),
                                                                        skill.getName(),
                                                                        category.getName(),
                                                                        empSkill.getCurrentLevel()
                                                                ))
                                                        )
                                                )
                                                .collectList()
                                                .flatMap(skillsInfo -> {
                                                    if (employee.getJobTitleId() == null) {
                                                        EmployeeSkillsResponse response = new EmployeeSkillsResponse(
                                                                employee.getId(),
                                                                employee.getFirstname(),
                                                                employee.getLastname(),
                                                                employee.getMatricule(),
                                                                null,
                                                                skillsInfo
                                                        );
                                                        return Mono.just(ResponseEntity.ok((Object) response));
                                                    } else {
                                                        return jobTitleRepository.findById(employee.getJobTitleId())
                                                                .map(jobTitle -> {
                                                                    EmployeeSkillsResponse response = new EmployeeSkillsResponse(
                                                                            employee.getId(),
                                                                            employee.getFirstname(),
                                                                            employee.getLastname(),
                                                                            employee.getMatricule(),
                                                                            jobTitle.getName(),
                                                                            skillsInfo
                                                                    );
                                                                    return ResponseEntity.ok((Object) response);
                                                                });
                                                    }
                                                })
                                )
                )
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Employee not found")) {
                        return Mono.just(
                                ResponseEntity.status(404)
                                        .body((Object) Map.of("error", "Employee not found"))
                        );
                    }
                    return Mono.error(e);
                });
    }



    public Flux<EmployeeSkill> findBySkillId(Long skillId) {
        return employeeSkillRepository.findBySkillId(skillId);
    }

    public Mono<EmployeeSkill> create(EmployeeSkill employeeSkill) {
        return employeeSkillRepository.save(employeeSkill);
    }

    public Flux<EmployeeSkill> createMultiple(Long employeeId, List<EmployeeSkillsRequest.SkillLevelDTO> skills) {
        return Flux.fromIterable(skills)
                .flatMap(skill -> {
                    EmployeeSkill employeeSkill = EmployeeSkill.builder()
                            .employeeId(employeeId)
                            .skillId(skill.getSkillId())
                            .currentLevel(skill.getCurrentLevel())
                            .build();
                    return create(employeeSkill);
                });
    }

    public Mono<EmployeeSkillUpdateResponse> updateSkillsSummary(Long employeeId, List<EmployeeSkillsRequest.SkillLevelDTO> skills) {

        return employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found")))
                .flatMap(employee -> {
                    List<EmployeeSkillUpdateResponse.SkillInfo> created = Collections.synchronizedList(new ArrayList<>());
                    List<EmployeeSkillUpdateResponse.SkillInfo> updated = Collections.synchronizedList(new ArrayList<>());
                    List<EmployeeSkillUpdateResponse.FailureInfo> failures = Collections.synchronizedList(new ArrayList<>());

                    return Flux.fromIterable(skills)
                            .concatMap(skillDto ->
                                    skillRepository.findById(skillDto.getSkillId())
                                            .flatMap(skill ->
                                                    employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skillDto.getSkillId())
                                                            .flatMap(existing ->
                                                                    employeeSkillRepository.updateSkillLevel(
                                                                                    employeeId,
                                                                                    skillDto.getSkillId(),
                                                                                    skillDto.getCurrentLevel()
                                                                            )
                                                                            .then(Mono.fromCallable(() -> {
                                                                                existing.setCurrentLevel(skillDto.getCurrentLevel());
                                                                                updated.add(EmployeeSkillUpdateResponse.SkillInfo.builder()
                                                                                        .id(existing.getSkillId())
                                                                                        .name(skill.getName())
                                                                                        .build());
                                                                                return existing;
                                                                            }))
                                                            )
                                                            .switchIfEmpty(
                                                                    Mono.defer(() ->
                                                                            employeeSkillRepository.save(EmployeeSkill.builder()
                                                                                            .employeeId(employeeId)
                                                                                            .skillId(skillDto.getSkillId())
                                                                                            .currentLevel(skillDto.getCurrentLevel())
                                                                                            .build())
                                                                                    .map(saved -> {
                                                                                        created.add(EmployeeSkillUpdateResponse.SkillInfo.builder()
                                                                                                .id(saved.getSkillId())
                                                                                                .name(skill.getName())
                                                                                                .build());
                                                                                        return saved;
                                                                                    })
                                                                    )
                                                            )
                                            )
                                            .switchIfEmpty(
                                                    Mono.fromRunnable(() -> {
                                                        // Skill not found
                                                        failures.add(EmployeeSkillUpdateResponse.FailureInfo.builder()
                                                                .skillId(skillDto.getSkillId())
                                                                .errorMessage("Skill not found")
                                                                .build());
                                                    }).then(Mono.empty())
                                            )
                            )
                            .collectList()
                            .map(list -> EmployeeSkillUpdateResponse.builder()
                                    .nbCreatedSkills(created.size())
                                    .createdSkills(created)
                                    .nbUpdatedSkills(updated.size())
                                    .updatedSkills(updated)
                                    .nbFailures(failures.size())
                                    .failureDetails(failures)
                                    .nbSuccess(created.size() + updated.size())
                                    .build()
                            );
                });
    }






    public Mono<Void> deleteByEmployeeId(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found")))
                .flatMap(employee -> employeeSkillRepository.deleteByEmployeeId(employeeId));
    }



    /**
     * Nouvelle version typée avec DTO EmployeeSkillImportResponse
     */
    public Mono<ResponseEntity<EmployeeSkillImportResponse>> importSkill(FilePart file) {
        // Étape 1 : Parser le fichier Excel en TempEmployeeSkill
        Flux<TempEmployeeSkill> tempSkills = parseWorkbook(file)
                .map(ts -> {
                    ts.employeeMatricule = ts.employeeMatricule.trim().toUpperCase();
                    ts.skillName = ts.skillName.trim().replaceAll("\\s+", " ");
                    return ts;
                });

        // Étape 2 : Traiter chaque TempEmployeeSkill séquentiellement
        Flux<ImportResult> results = tempSkills.concatMap(this::saveEmployeeSkill);

        // Étape 3 : Construire la réponse finale avec déduplication
        return results.collectList().map(list -> {

            // Déduplication des compétences créées
            List<SkillInfo> createdSkills = list.stream()
                    .filter(r -> "Saved".equals(r.action))
                    .map(r -> SkillInfo.builder()
                            .id(null)
                            .name(r.skillName)
                            .category(r.category != null ? r.category : "unknown")
                            .build())
                    .distinct()
                    .toList();

            // Déduplication des compétences mises à jour
            List<SkillInfo> updatedSkills = list.stream()
                    .filter(r -> "Updated".equals(r.action))
                    .map(r -> SkillInfo.builder()
                            .id(null)
                            .name(r.skillName)
                            .category(r.category != null ? r.category : "unknown")
                            .build())
                    .distinct()
                    .toList();

            // Déduplication des erreurs par matricule + message
            List<FailureDetail> failureDetails = list.stream()
                    .filter(r -> "Error".equals(r.action))
                    .map(r -> new FailureDetail(r.employeeMatricule, r.errorMessage))
                    .distinct() // ici on suppose que equals/hashCode sont sur matricule + errorMessage
                    .toList();

            // Calcul du nombre de succès après déduplication
            int nbSuccess = createdSkills.size() + updatedSkills.size();

            // Création de la réponse finale
            EmployeeSkillImportResponse response = EmployeeSkillImportResponse.builder()
                    .nbCreatedSkills(createdSkills.size())
                    .createdSkills(createdSkills)
                    .nbSuccess(nbSuccess)
                    .nbFailures(failureDetails.size())
                    .failureDetails(failureDetails)
                    .build();

            return ResponseEntity.ok(response);
        });
    }













    private Flux<TempEmployeeSkill> parseWorkbook(FilePart file) {
        return DataBufferUtils.join(file.content())
                .flatMapMany(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    List<TempEmployeeSkill> results = new ArrayList<>();
                    try (InputStream inputStream = new ByteArrayInputStream(bytes);
                         Workbook workbook = WorkbookFactory.create(inputStream)) {

                        Sheet sheet = workbook.getSheetAt(0);
                        Row header = sheet.getRow(0);
                        if (header == null) return Flux.fromIterable(results);

                        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row == null) continue;

                            String rawMatricule = getStringCellValue(row.getCell(0));
                            if (rawMatricule == null) continue;
                            String matricule = rawMatricule.split(":")[0].trim();

                            for (int col = 1; col < row.getLastCellNum(); col++) {
                                String skillName = getStringCellValue(header.getCell(col));
                                if (skillName != null) {
                                    skillName = skillName.trim().replaceAll("\\s+", " ");
                                }
                                Double level = getDoubleCellValue(row.getCell(col));
                                if (skillName != null && level != null) {
                                    results.add(new TempEmployeeSkill(
                                            matricule,
                                            skillName,
                                            mapFloatToSkillLevel(level),
                                            i + 1 // row number (1-based)
                                    ));
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse workbook: {}", e.getMessage(), e);
                    }
                    return Flux.fromIterable(results);
                });
    }

    private Mono<ImportResult> saveEmployeeSkill(TempEmployeeSkill tempSkill) {
        String matricule = tempSkill.employeeMatricule.trim().toUpperCase();
        String skillName = tempSkill.skillName.trim().replaceAll("\\s+", " ");

        // Chercher ou créer la skill
        Mono<Skill> skillMono = skillRepository.findByNameIgnoreCase(skillName)
                .switchIfEmpty(Mono.defer(() ->
                        skillCategoryRepository.findByNameIgnoreCase("unknown")
                                .switchIfEmpty(skillCategoryRepository.save(
                                        SkillCategory.builder().name("unknown").build()
                                ))
                                .flatMap(cat -> {
                                    Skill newSkill = new Skill();
                                    newSkill.setName(skillName);
                                    newSkill.setSkillCategoryId(cat.getId());
                                    return skillRepository.save(newSkill);
                                })
                ));

        return skillMono.flatMap(skill ->
                        // Tenter un update par matricule
                        employeeSkillRepository.updateSkillLevelByMatricule(matricule, skill.getId(), tempSkill.currentLevel)
                                .flatMap(rowsUpdated -> {
                                    if (rowsUpdated > 0) {
                                        return Mono.just(new ImportResult(
                                                matricule,
                                                skill.getName(),
                                                "Updated",
                                                null,
                                                tempSkill.row,
                                                skill.getSkillCategoryId() != null ? "known" : "unknown"
                                        ));
                                    } else {
                                        // L'employé existe ?
                                        return employeeRepository.findByMatriculeIgnoreCase(matricule)
                                                .flatMap(employee -> {
                                                    EmployeeSkill es = new EmployeeSkill();
                                                    es.setEmployeeId(employee.getId());
                                                    es.setSkillId(skill.getId());
                                                    es.setCurrentLevel(tempSkill.currentLevel);
                                                    return employeeSkillRepository.save(es)
                                                            .map(saved -> new ImportResult(
                                                                    matricule,
                                                                    skill.getName(),
                                                                    "Saved",
                                                                    null,
                                                                    tempSkill.row,
                                                                    skill.getSkillCategoryId() != null ? "known" : "unknown"
                                                            ));
                                                })
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    // Matricule inexistant → erreur
                                                    return Mono.just(new ImportResult(
                                                            matricule,
                                                            skill.getName(),
                                                            "Error",
                                                            "Employee not found",
                                                            tempSkill.row,
                                                            "unknown"
                                                    ));
                                                }));
                                    }
                                })
                )
                .onErrorResume(e -> Mono.just(new ImportResult(
                        matricule,
                        skillName,
                        "Error",
                        e.getMessage(),
                        tempSkill.row,
                        "unknown"
                )));
    }








    private Mono<Skill> getSkillByName(String name) {
        if (name == null) {
            return Mono.error(new RuntimeException("Skill name is null"));
        }
        String normalizedName = name.trim().replaceAll("\\s+", " ");
        return skillRepository.findByNameIgnoreCase(normalizedName)
                .switchIfEmpty(Mono.error(new RuntimeException("Skill not found: " + normalizedName)));
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private Double getDoubleCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Double.parseDouble(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            log.warn("Error parsing number: {}", e.getMessage());
        }
        return null;
    }

    private String mapFloatToSkillLevel(double level) {
        if (level >= 1 && level < 2) return "Basic";
        if (level >= 2 && level < 3) return "Competent";
        if (level >= 3 && level < 4) return "Advanced";
        if (level >= 4) return "Expert";
        return "Unknown";
    }

    public Flux<?> searchEmployees(
            List<FilterRequest> skillFilters,
            List<FilterRequest> employeeFilters,
            int offset,
            int limit) {


        boolean hasSkillFilters = skillFilters != null && !skillFilters.isEmpty();
        boolean hasEmployeeFilters = employeeFilters != null && !employeeFilters.isEmpty();

        if (!hasSkillFilters && !hasEmployeeFilters) {
            return Flux.empty();
        }

        // Cas uniquement skills
        if (hasSkillFilters && !hasEmployeeFilters) {

            return findEmployeesBySkills(skillFilters, offset, limit);
        }

//        // Cas uniquement attributs employés (avec skills)
        if (!hasSkillFilters && hasEmployeeFilters) {
            return findEmployeesByAttributesWithSkills(employeeFilters, offset, limit);
        }


        // Cas combiné : skills + attributs employés
        return findEmployeesBySkillsRaw(skillFilters)
                .collectList()
                .flatMapMany(employees ->
                        jobTitleRepository.findAll()
                                .collectMap(JobTitle::getId, JobTitle::getName)
                                .flatMapMany(jobTitleMap -> Flux.fromIterable(employees)
                                        .filter(emp -> matchEmployeeFiltersInMemory(emp, employeeFilters, jobTitleMap))
                                        .flatMap(this::mapToDto) // mapping DTO après filtrage
                                        .skip(offset)
                                        .take(limit)
                                )
                );

    }
    // 🔹 Recherche par skills (raw, sans mapping DTO)
    private Flux<Employee> findEmployeesBySkillsRaw(List<FilterRequest> skillFilters) {
        return Flux.fromIterable(skillFilters)
                .flatMap(filter ->
                        skillRepository.findByNameIgnoreCase(filter.getName())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Skill not found: '" + filter.getName() + "'"
                                )))
                                .flatMapMany(skill -> employeeSkillRepository.findBySkillId(skill.getId())
                                        .filter(es -> applyOperator(es.getCurrentLevel(), filter.getOperator(), filter.getValue()))
                                        .map(EmployeeSkill::getEmployeeId)
                                )
                )
                .collectMultimap(empId -> empId)
                .flatMapMany(empMap -> {
                    List<Long> empIds = empMap.entrySet().stream()
                            .filter(e -> e.getValue().size() == skillFilters.size())
                            .map(Map.Entry::getKey)
                            .toList();
                    return employeeRepository.findAllById(empIds);
                });
    }


    // 🔹 Recherche par skills (privée)
    private Flux<EmployeeBasicInfoDTO> findEmployeesBySkills(List<FilterRequest> skillFilters, int offset, int limit) {

        return Flux.fromIterable(skillFilters)
                .flatMap(filter ->
                        skillRepository.findByNameIgnoreCase(filter.getName())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Skill not found: '" + filter.getName() + "'"
                                )))
                                .flatMapMany(skill -> employeeSkillRepository.findBySkillId(skill.getId())
                                        .filter(es -> applyOperator(es.getCurrentLevel(), filter.getOperator(), filter.getValue()))
                                        .map(EmployeeSkill::getEmployeeId)
                                )
                )
                .collectMultimap(empId -> empId)
                .map(empMap -> empMap.entrySet().stream()
                        .filter(e -> e.getValue().size() == skillFilters.size())
                        .map(Map.Entry::getKey)
                        .toList()
                )
                .flatMapMany(empIds -> employeeRepository.findAllById(empIds)
                        .skip(offset)
                        .take(limit)
                        .flatMap(this::mapToDto)
                );
    }

    // 🔹 Recherche par attributs employés (privée)
    private Flux<EmployeeBasicInfoDTO> findEmployeesByAttributes(List<FilterRequest> filters, int offset, int limit) {
        return employeeRepository.findAll()
                .filterWhen(emp -> matchEmployeeFilters(emp, filters))
                .skip(offset)
                .take(limit)
                .flatMap(this::mapToDto);
    }

    // 🔹 Filtrage asynchrone pour attributs seuls
    private Mono<Boolean> matchEmployeeFilters(Employee emp, List<FilterRequest> filters) {
        return Flux.fromIterable(filters)
                .flatMap(filter -> {
                    switch (filter.getName().toLowerCase()) {
                        case "jobtitle":
                            return jobTitleRepository.findById(emp.getJobTitleId())
                                    .map(job -> applyOperator(job.getName(), filter.getOperator(), filter.getValue()))
                                    .defaultIfEmpty(false);
                        case "matricule":
                            return Mono.just(applyOperator(emp.getMatricule(), filter.getOperator(), filter.getValue()));
                        case "firstname":
                            return Mono.just(applyOperator(emp.getFirstname(), filter.getOperator(), filter.getValue()));
                        case "lastname":
                            return Mono.just(applyOperator(emp.getLastname(), filter.getOperator(), filter.getValue()));
                        default:
                            return Mono.just(false);
                    }
                })
                .all(Boolean::booleanValue);
    }

    // 🔹 Filtrage en mémoire pour JobTitle préchargé (utilisé dans cas combiné)
    private boolean matchEmployeeFiltersInMemory(Employee emp, List<FilterRequest> filters, Map<Long, String> jobTitleMap) {
        for (FilterRequest filter : filters) {
            String field = filter.getName().toLowerCase();
            boolean matched = switch (field) {
                case "jobtitle" -> applyOperator(jobTitleMap.get(emp.getJobTitleId()), filter.getOperator(), filter.getValue());
                case "matricule" -> applyOperator(emp.getMatricule(), filter.getOperator(), filter.getValue());
                case "firstname" -> applyOperator(emp.getFirstname(), filter.getOperator(), filter.getValue());
                case "lastname" -> applyOperator(emp.getLastname(), filter.getOperator(), filter.getValue());
                default -> false;
            };

            if (!matched) return false;
        }
        return true;
    }

    // 🔹 Mapping DTO
    private Mono<EmployeeBasicInfoDTO> mapToDto(Employee emp) {
        return jobTitleRepository.findById(emp.getJobTitleId())
                .map(JobTitle::getName)
                .defaultIfEmpty("Unknown")
                .map(jobTitle -> new EmployeeBasicInfoDTO(
                        emp.getId(),
                        emp.getFirstname(),
                        emp.getLastname(),
                        emp.getMatricule(),
                        jobTitle
                ));
    }



    // 🔹 Comparaison des opérateurs
    private boolean applyOperator(String employeeValue, String operator, String filterValue) {
        if (employeeValue == null) return false;

        return switch (operator.toLowerCase()) {
            case "equals" -> employeeValue.equalsIgnoreCase(filterValue);
            case "greaterthan" -> compareLevels(employeeValue, filterValue) > 0;
            case "lowerthan" -> compareLevels(employeeValue, filterValue) < 0;
            default -> false;
        };
    }

    private int compareLevels(String empLevel, String filterLevel) {
        List<String> levels = List.of("Basic", "Competent", "Advanced", "Expert");
        int empIndex = levels.indexOf(capitalize(empLevel));
        int filterIndex = levels.indexOf(capitalize(filterLevel));
        if (empIndex == -1 || filterIndex == -1) return 0;
        return Integer.compare(empIndex, filterIndex);
    }

    private String capitalize(String str) {
        if (str == null || str.isBlank()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // 🔹 Recherche par attributs employés avec DTO EmployeeFiltersResponse
    private Flux<EmployeeFiltersResponse> findEmployeesByAttributesWithSkills(List<FilterRequest> filters, int offset, int limit) {
        return employeeRepository.findAll()
                .filterWhen(emp -> matchEmployeeFilters(emp, filters))
                .skip(offset)
                .take(limit)
                .flatMap(emp ->
                        employeeSkillRepository.findByEmployeeId(emp.getId())
                                .flatMap(es ->
                                        skillRepository.findById(es.getSkillId())
                                                .flatMap(skill ->
                                                        skillCategoryRepository.findById(skill.getSkillCategoryId())
                                                                .map(cat -> SkillInfo.builder()
                                                                        .id(skill.getId())
                                                                        .name(skill.getName())
                                                                        .category(cat.getName()) // récupère le nom de la catégorie
                                                                        .currentLevel(es.getCurrentLevel())
                                                                        .build()
                                                                )
                                                )
                                )
                                .collectList()
                                .map(skills -> EmployeeFiltersResponse.builder()
                                        .employeeId(emp.getId())
                                        .firstname(emp.getFirstname())
                                        .lastname(emp.getLastname())
                                        .matricule(emp.getMatricule())
                                        .skills(skills)
                                        .build()
                                )
                );
    }



}
