package edu.eci.cvds.proyect.coliseum.persistency.controller;

import edu.eci.cvds.proyect.coliseum.persistency.exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/Loan")
@Tag(
        name = "Préstamos",
        description = "Gestión integral de préstamos de artículos deportivos (balones, raquetas, lazos, etc.)"
)
@Validated
@Slf4j
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private static final String ERROR_KEY = "Error";
    private static final String MESSAGE_KEY = "Message";
    private static final String HOURLY_LOAN_PREFIX = "[Préstamo por horas: ";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final LoanService loanService;
    private final AlertRepository alertRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public LoanController(LoanService loanService, AlertRepository alertRepository, ArticleRepository articleRepository) {
        this.loanService = Objects.requireNonNull(loanService, "loanService must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "alertRepository must not be null");
        this.articleRepository = Objects.requireNonNull(articleRepository, "articleRepository must not be null");
    }

    @GetMapping
    @Operation(
            summary = "Obtener préstamos",
            description = """
            Recupera préstamos con múltiples criterios de búsqueda:
            - Sin parámetros: Todos los préstamos
            - ID formato LN-xxx: Préstamo específico
            - ID formato U-xxx: Préstamos de un usuario
            - Estado: 'Prestado', 'Devuelto', 'Vencido'
            - Fechas: 'fechas:yyyy-MM-dd:yyyy-MM-dd'
            - Tipo: 'tipo:nombreTipo' (ej: tipo:Balon)
            - Horas: 'horas:true' (préstamos por horas)
            """,
            parameters = {
                    @Parameter(
                            name = "q",
                            description = """
                            Criterio de búsqueda:
                            - LN-xxx: ID del préstamo
                            - U-xxx: ID del usuario
                            - Prestado/Devuelto/Vencido: Estado
                            - fechas:2024-01-01:2024-02-01: Rango de fechas
                            - tipo:Balon: Búsqueda por tipo de equipo
                            - horas:true: Préstamos por horas
                            """,
                            examples = {
                                    @ExampleObject(name = "Todos", value = ""),
                                    @ExampleObject(name = "Por ID", value = "LN-5f5b3b3b1f1b3b5f5b3b3b1f"),
                                    @ExampleObject(name = "Por usuario", value = "U-12345"),
                                    @ExampleObject(name = "Por estado", value = "Prestado"),
                                    @ExampleObject(name = "Por fechas", value = "fechas:2024-01-01:2024-02-01"),
                                    @ExampleObject(name = "Por tipo", value = "tipo:Balon"),
                                    @ExampleObject(name = "Por horas", value = "horas:true")
                            }
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Resultados de búsqueda",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Todos los préstamos",
                                                    value = """
                            {
                                "cantidad": 2,
                                "prestamos": [
                                    {
                                        "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                        "userId": "U-12345",
                                        "nameUser": "Juan Pérez",
                                        "loanStatus": "Prestado",
                                        "loanDate": "2024-03-15",
                                        "devolutionDate": "2024-03-22",
                                        "articleIds": [101, 102],
                                        "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                                    },
                                    {
                                        "id": "LN-6a6c4c4c2a2c4c6a6c4c4c2a",
                                        "userId": "U-54321",
                                        "nameUser": "Ana Gómez",
                                        "loanStatus": "Devuelto",
                                        "loanDate": "2024-02-10",
                                        "devolutionDate": "2024-02-17",
                                        "articleIds": [103, 104],
                                        "loanDescriptionType": "Préstamo para torneo"
                                    }
                                ]
                            }
                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros inválidos",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "Error": "Error al obtener préstamos",
                            "Message": "Formato de fechas inválido"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> getLoans(@RequestParam(value = "q", required = false) String q) {
        try {
            List<Loan> result;

            if (q == null || q.isBlank()) {
                // Obtener todos los préstamos
                result = loanService.getLoans(null);
            } else if (q.startsWith("LN-") || ObjectId.isValid(q)) {
                // Búsqueda por ID - acepta tanto IDs con prefijo LN- como ObjectIds de MongoDB
                String searchId = q.startsWith("LN-") ? q.substring(3) : q;
                Loan loan = loanService.getLoanById(searchId);
                result = (loan != null) ? List.of(loan) : List.of();
            } else if (q.startsWith("U-") || q.matches("\\d+")) {
                // Búsqueda por userId
                result = loanService.getLoansByUserReport(q);
            } else if (q.startsWith("fechas:")) {
                // Búsqueda por rango de fechas: "fechas:2024-01-01:2024-02-01"
                String[] parts = q.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Formato de fechas inválido, use: fechas:yyyy-MM-dd:yyyy-MM-dd");
                }

                LocalDate startDate = LocalDate.parse(parts[1]);
                LocalDate endDate = LocalDate.parse(parts[2]);

                result = loanService.getLoansByDateRangeAndStatus(startDate, endDate, null);
            } else if (q.startsWith("tipo:")) {
                // Búsqueda por tipo de equipo
                String tipoEquipo = q.substring("tipo:".length());
                result = getLoansByEquipmentType(tipoEquipo);
            } else if (q.startsWith("horas:")) {
                // NUEVA FUNCIONALIDAD: Búsqueda de préstamos por horas
                boolean onlyHourly = Boolean.parseBoolean(q.substring("horas:".length()));
                result = getHourlyLoans(onlyHourly);
            } else {
                // Considerar la búsqueda por estado: "Prestado", "Devuelto", "Vencido"
                Set<String> estadosValidos = Set.of("Prestado", "Devuelto", "Vencido");

                if (estadosValidos.contains(q)) {
                    result = loanService.getLoans(q);
                } else {
                    // Si no es un estado válido, buscar por nombre de usuario
                    result = loanService.getLoans(null).stream()
                            .filter(loan -> loan.getNameUser() != null &&
                                    loan.getNameUser().toLowerCase().contains(q.toLowerCase()))
                            .collect(Collectors.toList());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("cantidad", result.size());
            response.put("prestamos", result);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (LoanException e) {
            logger.error("Error al buscar préstamo: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al buscar préstamo");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.error("Parámetros inválidos: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al obtener préstamos");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error interno al obtener préstamos: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al obtener préstamos");
            errorResponse.put(MESSAGE_KEY, "Error interno del servidor");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Método para obtener préstamos por tipo de equipo
    private List<Loan> getLoansByEquipmentType(String equipmentType) {
        // Utilizamos findByName que devuelve un Optional<List<Article>>
        Optional<List<Article>> articlesOpt = articleRepository.findByName(equipmentType);

        if (articlesOpt.isEmpty()) {
            logger.info("No se encontraron artículos del tipo: {}", equipmentType);
            return Collections.emptyList();
        }

        // Extraemos la lista de artículos y luego obtenemos sus IDs
        List<Integer> articleIds = articlesOpt.get().stream()
                .map(article -> article.getId())  // Usamos lambda en lugar de method reference
                .collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            logger.info("No se encontraron artículos del tipo: {}", equipmentType);
            return Collections.emptyList();
        }

        // Filtramos los préstamos
        return loanService.getLoans(null).stream()
                .filter(loan -> {
                    if (loan.getArticleIds() == null || loan.getArticleIds().isEmpty()) {
                        return false;
                    }
                    return loan.getArticleIds().stream()
                            .anyMatch(articleIds::contains);
                })
                .collect(Collectors.toList());
    }

    // Método para obtener préstamos por horas (usa el campo loanDescriptionType)
    private List<Loan> getHourlyLoans(boolean onlyHourly) {
        List<Loan> allLoans = loanService.getLoans(null);

        return allLoans.stream()
                .filter(loan -> {
                    boolean isHourlyLoan = loan.getLoanDescriptionType() != null &&
                            loan.getLoanDescriptionType().startsWith(HOURLY_LOAN_PREFIX);
                    return onlyHourly ? isHourlyLoan : !isHourlyLoan;
                })
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(
            summary = "Crear préstamo",
            description = "Registra un nuevo préstamo de artículos deportivos. Puede ser por días u horas.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del préstamo a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "articleIds": [101, 102],
                        "nameUser": "Juan Pérez",
                        "userId": "U-12345",
                        "userRole": "Estudiante",
                        "loanDescriptionType": "Préstamo para torneo",
                        "loanDate": "2024-03-15",
                        "devolutionDate": "2024-03-22",
                        "loanStatus": "Prestado",
                        "equipmentStatus": "En buen estado"
                    }
                    """
                            )
                    )
            ),
            parameters = {
                    @Parameter(name = "hourlyLoan", description = "Indica si es un préstamo por horas", required = false),
                    @Parameter(name = "startTime", description = "Hora de inicio (HH:MM) para préstamos por horas", required = false),
                    @Parameter(name = "endTime", description = "Hora de fin (HH:MM) para préstamos por horas", required = false)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Préstamo creado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                            "articleIds": [101, 102],
                            "nameUser": "Juan Pérez",
                            "loanStatus": "Prestado",
                            "loanDate": "2024-03-15",
                            "devolutionDate": "2024-03-15",
                            "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error de validación",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Artículos no disponibles",
                                                    value = """ 
                            { 
                                "Error": "Error al crear préstamo",
                                "Message": "Los siguientes artículos no están disponibles: [101]" 
                            }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Formato de hora inválido",
                                                    value = """ 
                            { 
                                "Error": "Error al crear préstamo",
                                "Message": "Formato de hora inválido. Use HH:MM" 
                            }"""
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<Object> save(
            @Valid @RequestBody Loan loan,
            @RequestParam(value = "hourlyLoan", required = false, defaultValue = "false") boolean hourlyLoan,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {

        logger.info("Creando nuevo préstamo para usuario: {}, por horas: {}", loan.getUserId(), hourlyLoan);
        try {
            // Validar que los artículos estén disponibles y no dañados
            validateArticlesAvailable(loan.getArticleIds());

            // Modificar el préstamo si es por horas
            if (hourlyLoan) {
                handleHourlyLoan(loan, startTime, endTime);
            }

            Loan createdLoan = loanService.createLoan(loan);
            logger.info("Préstamo creado con ID: {}", createdLoan.getId());
            return new ResponseEntity<>(createdLoan, HttpStatus.CREATED);
        } catch (LoanException | ArticleException e) {
            logger.error("Error al crear préstamo: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al crear préstamo");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error interno al crear préstamo: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al crear préstamo");
            errorResponse.put(MESSAGE_KEY, "Error interno del servidor");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateArticlesAvailable(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos un artículo");
        }

        List<Article> articles = articleRepository.findAllById(articleIds);

        if (articles.size() != articleIds.size()) {
            throw new IllegalArgumentException("Algunos artículos no existen");
        }

        List<Integer> unavailableArticles = articles.stream()
                .filter(article -> !"Disponible".equals(article.getArticleStatus()))
                .map(Article::getId)
                .toList();

        if (!unavailableArticles.isEmpty()) {
            throw new IllegalArgumentException("Los siguientes artículos no están disponibles o están dañados: " + unavailableArticles);
        }
    }

    private void handleHourlyLoan(Loan loan, String startTimeStr, String endTimeStr) {
        if (startTimeStr == null || endTimeStr == null) {
            throw new IllegalArgumentException("Para préstamos por horas debe especificar startTime y endTime");
        }

        LocalTime startTime;
        LocalTime endTime;

        try {
            startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
            endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de hora inválido. Use HH:MM", e);
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
        }

        // Si la fecha de préstamo no se especificó, usar hoy
        if (loan.getLoanDate() == null) {
            loan.setLoanDate(LocalDate.now());
        }

        // Para préstamos por horas, la fecha de devolución es la misma que la de préstamo
        loan.setDevolutionDate(loan.getLoanDate());

        // Guardar información de horas en la descripción del préstamo
        String originalDescription = loan.getLoanDescriptionType() != null ? loan.getLoanDescriptionType() : "";
        if (!originalDescription.startsWith(HOURLY_LOAN_PREFIX)) {
            loan.setLoanDescriptionType(HOURLY_LOAN_PREFIX + startTimeStr + "-" + endTimeStr + "] " + originalDescription);
        }
    }

    @GetMapping("/reports")
    @Operation(
            summary = "Generar reportes",
            description = "Genera reportes especializados de préstamos",
            parameters = {
                    @Parameter(
                            name = "type",
                            description = "Tipo de reporte: student, equipment, status, dateRange, hourly",
                            required = true,
                            example = "equipment"
                    ),
                    @Parameter(
                            name = "value",
                            description = "Valor para filtrar el reporte",
                            example = "Balon"
                    ),
                    @Parameter(
                            name = "startDate",
                            description = "Fecha de inicio para reportes por rango",
                            example = "2024-01-01"
                    ),
                    @Parameter(
                            name = "endDate",
                            description = "Fecha de fin para reportes por rango",
                            example = "2024-12-31"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte generado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "reportType": "equipment",
                            "reportValue": "Balon",
                            "totalItems": 5,
                            "data": [
                                {
                                    "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                    "userId": "U-12345",
                                    "nameUser": "Juan Pérez",
                                    "loanDate": "2024-03-15",
                                    "devolutionDate": "2024-03-15",
                                    "loanStatus": "Prestado",
                                    "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                                }
                            ]
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros inválidos",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "Error": "Error al generar reporte",
                            "Message": "Tipo de reporte inválido. Use: student, equipment, status, hourly, o dateRange"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> generateReport(
            @RequestParam("type") String reportType,
            @RequestParam(value = "value", required = false) String value,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        try {
            Map<String, Object> report = new HashMap<>();
            List<Loan> reportData = Collections.emptyList();

            report.put("reportType", reportType);

            switch (reportType.toLowerCase()) {
                case "student":
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException("Debe proporcionar un ID de estudiante");
                    }
                    reportData = loanService.getLoansByUserReport(value);
                    report.put("reportValue", value);
                    break;

                case "equipment":
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException("Debe proporcionar un tipo de equipo");
                    }
                    reportData = getLoansByEquipmentType(value);
                    report.put("reportValue", value);
                    break;

                case "status":
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException("Debe proporcionar un estado");
                    }
                    reportData = loanService.getLoans(value);
                    report.put("reportValue", value);
                    break;

                case "hourly":
                    boolean onlyHourly = value == null || "true".equalsIgnoreCase(value);
                    reportData = getHourlyLoans(onlyHourly);
                    report.put("reportValue", onlyHourly ? "Por horas" : "Por días");
                    break;

                case "daterange":
                    if (startDate == null || endDate == null) {
                        throw new IllegalArgumentException("Debe proporcionar fechas de inicio y fin");
                    }
                    reportData = loanService.getLoansByDateRangeAndStatus(startDate, endDate, null);
                    report.put("startDate", startDate);
                    report.put("endDate", endDate);
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Tipo de reporte inválido. Use: student, equipment, status, hourly, o dateRange");
            }

            report.put("totalItems", reportData.size());
            report.put("data", reportData);

            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al generar reporte");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al generar reporte");
            errorResponse.put(MESSAGE_KEY, "Error interno: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar préstamo",
            description = "Actualización completa de un préstamo existente",
            parameters = @Parameter(
                    name = "id",
                    description = "ID del préstamo",
                    example = "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                    required = true
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del préstamo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                        {
                            "equipmentStatus": "Dañado",
                            "loanStatus": "Devuelto",
                            "articleIds": [101, 102],
                            "devolutionDate": "2024-03-22"
                        }
                        """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Préstamo actualizado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                            "nameUser": "Juan Pérez",
                            "equipmentStatus": "Dañado",
                            "loanStatus": "Devuelto",
                            "articleIds": [101, 102],
                            "devolutionDate": "2024-03-22"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error en actualización",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "Error": "Error al actualizar préstamo",
                            "Message": "No se puede modificar un préstamo ya devuelto"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Object> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates,
            @RequestParam(value = "hourlyLoan", required = false) Boolean isHourlyLoan,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {

        logger.info("Actualizando préstamo con ID: {}, campos: {}", id, updates.keySet());
        try {
            // Verificar si es una devolución completa
            if (updates.containsKey("devolver") && Boolean.TRUE.equals(updates.get("devolver"))) {
                loanService.devolverLoan(id);
                Loan updatedLoan = loanService.getLoanById(id);
                return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
            }

            // Actualizar información de horas si es necesario
            if (Boolean.TRUE.equals(isHourlyLoan) && (startTime != null || endTime != null)) {
                Loan loan = loanService.getLoanById(id);

                // Extraer horas actuales de la descripción si existe
                String currentStartTime = null;
                String currentEndTime = null;

                if (loan.getLoanDescriptionType() != null && loan.getLoanDescriptionType().startsWith(HOURLY_LOAN_PREFIX)) {
                    String timeInfo = loan.getLoanDescriptionType()
                            .substring(HOURLY_LOAN_PREFIX.length())
                            .split("]")[0];
                    String[] times = timeInfo.split("-");
                    if (times.length == 2) {
                        currentStartTime = times[0];
                        currentEndTime = times[1];
                    }
                }

                // Actualizar con los nuevos valores o mantener los actuales
                String newStartTime = startTime != null ? startTime : currentStartTime;
                String newEndTime = endTime != null ? endTime : currentEndTime;

                if (newStartTime != null && newEndTime != null) {
                    try {
                        LocalTime start = LocalTime.parse(newStartTime, TIME_FORMATTER);
                        LocalTime end = LocalTime.parse(newEndTime, TIME_FORMATTER);

                        if (start.isAfter(end)) {
                            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
                        }

                        // Extraer descripción original
                        String originalDesc = loan.getLoanDescriptionType();
                        if (originalDesc != null && originalDesc.startsWith(HOURLY_LOAN_PREFIX)) {
                            int endBracketPos = originalDesc.indexOf(']');
                            if (endBracketPos >= 0 && endBracketPos + 1 < originalDesc.length()) {
                                originalDesc = originalDesc.substring(endBracketPos + 1).trim();
                            } else {
                                originalDesc = "";
                            }
                        }

                        // Actualizar descripción con nuevas horas
                        String updatedDesc = HOURLY_LOAN_PREFIX + newStartTime + "-" + newEndTime + "] " + originalDesc;
                        updates.put("loanDescriptionType", updatedDesc);

                        // Asegurarse que la fecha de devolución sea la misma que la de préstamo
                        updates.put("devolutionDate", loan.getLoanDate());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Formato de hora inválido. Use HH:MM", e);
                    }
                }
            }

            // Manejar la actualización de artículos individuales
            Map<String, String> articulosUpdate = null;
            if (updates.containsKey("articulos")) {
                articulosUpdate = extractArticulosMap(updates.get("articulos"));
                loanService.updateArticlesStatus(id, articulosUpdate);
                updates.remove("articulos");
            }

            // Actualizar campos generales del préstamo
            if (!updates.isEmpty()) {
                loanService.updateLoan(id, updates);
            }

            // Devolver el préstamo actualizado
            Loan updatedLoan = loanService.getLoanById(id);
            return new ResponseEntity<>(updatedLoan, HttpStatus.OK);

        } catch (LoanException e) {
            logger.error("Error al actualizar préstamo {}: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al actualizar préstamo");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.error("Datos inválidos para actualizar préstamo {}: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al actualizar préstamo");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error interno al actualizar préstamo {}: {}", id, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al actualizar préstamo");
            errorResponse.put(MESSAGE_KEY, "Error interno del servidor");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar préstamo",
            description = "Elimina un préstamo activo que no esté devuelto o vencido",
            parameters = @Parameter(
                    name = "id",
                    description = "ID único del préstamo",
                    example = "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                    required = true
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Préstamo eliminado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "message": "Préstamo eliminado correctamente"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No se puede eliminar el préstamo",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Préstamo devuelto",
                                                    value = """ 
                            { 
                                "Error": "Error al eliminar préstamo",
                                "Message": "No se puede eliminar un préstamo devuelto" 
                            }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Préstamo vencido",
                                                    value = """ 
                            { 
                                "Error": "Error al eliminar préstamo",
                                "Message": "No se puede eliminar un préstamo vencido" 
                            }"""
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<Object> delete(@PathVariable String id) {
        logger.info("Solicitando eliminar préstamo con ID: {}", id);
        try {
            loanService.deleteLoanById(id);
            logger.info("Préstamo eliminado exitosamente: {}", id);
            return new ResponseEntity<>(Collections.singletonMap("message", "Préstamo eliminado correctamente"), HttpStatus.OK);
        } catch (LoanException e) {
            logger.error("Error al eliminar préstamo {}: {}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al eliminar préstamo");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.error("ID de préstamo inválido: {}", id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al eliminar préstamo");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error interno al eliminar préstamo {}: {}", id, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al eliminar préstamo");
            errorResponse.put(MESSAGE_KEY, "Error interno del servidor");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/alerts")
    @Operation(
            summary = "Obtener alertas de préstamos",
            description = "Recupera alertas relacionadas con préstamos con opciones de filtrado y ordenamiento",
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "ID del usuario para filtrar alertas (opcional)",
                            example = "U-12345"
                    ),
                    @Parameter(
                            name = "days",
                            description = "Filtrar alertas de los últimos N días (opcional)",
                            example = "7"
                    ),
                    @Parameter(
                            name = "page",
                            description = "Número de página (empieza en 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Tamaño de la página",
                            example = "10"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Alertas obtenidas",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "totalAlertas": 2,
                        "page": 0,
                        "size": 10,
                        "alertas": [
                            {
                                "id": "65a1f3e8d4e8b10c9c8b4567",
                                "description": "U-12345",
                                "message": "Préstamo marcado como vencido: LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                "timestamp": "2025-05-10T10:30:45"
                            },
                            {
                                "id": "65a1f3e8d4e8b10c9c8b4568",
                                "description": "U-12345",
                                "message": "Recordatorio: Devolución pendiente para mañana (2025-05-12)",
                                "timestamp": "2025-05-10T09:00:00"
                            }
                        ]
                    }
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Error al obtener alertas",
                        "Message": "Error de conexión a la base de datos"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> getLoanAlerts(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        logger.info("Obteniendo alertas de préstamos, filtros: userId={}, days={}, page={}, size={}",
                userId, days, page, size);

        try {
            List<Alert> alerts;
            List<String> loanKeywords = Arrays.asList("préstamo", "prestamo", "devolución", "devolucion", "vencido");

            // Si se especifica un usuario, usamos el método especializado del repositorio
            if (userId != null && !userId.trim().isEmpty()) {
                // Se filtra por el contenido del mensaje que contenga userId
                alerts = alertRepository.findByMessageContainingIgnoreCase(userId);
            } else {
                // Obtenemos todas las alertas
                alerts = alertRepository.findAll();
            }

            // Filtramos alertas relacionadas con préstamos
            List<Alert> loanAlerts = alerts.stream()
                    .filter(alert -> {
                        String messageLower = alert.getMessage().toLowerCase();
                        return loanKeywords.stream()
                                .anyMatch(messageLower::contains);
                    })
                    .collect(Collectors.toList());

            // Aplicamos filtro por días si se especifica
            if (days != null && days > 0) {
                LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
                loanAlerts = loanAlerts.stream()
                        .filter(alert -> alert.getTimestamp().isAfter(cutoffDate))
                        .collect(Collectors.toList());
            }

            // Ordenamos por fecha más reciente primero
            loanAlerts.sort(Comparator.comparing(Alert::getTimestamp).reversed());

            int total = loanAlerts.size();
            int fromIndex = page * size;

            // Paginación manual simple
            if (fromIndex >= total) {
                loanAlerts = Collections.emptyList();
            } else {
                int toIndex = Math.min(fromIndex + size, total);
                loanAlerts = loanAlerts.subList(fromIndex, toIndex);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalAlertas", total);
            response.put("page", page);
            response.put("size", size);
            response.put("alertas", loanAlerts);

            logger.info("Devolviendo {} alertas de préstamos (de un total de {})", loanAlerts.size(), total);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al obtener alertas de préstamos: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al obtener alertas");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    Map<String, String> extractArticulosMap(Object articulosObj) {
        if (articulosObj == null) {
            return Collections.emptyMap();
        }

        if (!(articulosObj instanceof Map)) {
            throw new IllegalArgumentException("El campo 'articulos' debe ser un objeto JSON válido");
        }

        try {
            Map<String, Object> articulos = (Map<String, Object>) articulosObj;
            Map<String, String> result = new HashMap<>();

            for (Map.Entry<String, Object> entry : articulos.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    throw new IllegalArgumentException("Los IDs de artículo y estados no pueden ser nulos");
                }

                // Validar que el ID del artículo es un número
                try {
                    Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("ID de artículo inválido: " + entry.getKey());
                }

                result.put(entry.getKey(), entry.getValue().toString());
            }

            return result;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Formato inválido para 'articulos': " + e.getMessage());
        }
    }
}