package edu.eci.cvds.proyect.coliseum.persistency.controller;

import edu.eci.cvds.proyect.coliseum.persistency.exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanArticleService;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/LoanArticle")
@Tag(
        name = "Préstamos",
        description = "Gestión integral de préstamos de artículos deportivos (balones, raquetas, lazos, etc.)"
)
@Validated
@Slf4j
public class LoanArticleController {

    private static final Logger logger = LoggerFactory.getLogger(LoanArticleController.class);

    private static final String ERROR_KEY = "Error";
    private static final String MESSAGE_KEY = "Message";
    private static final String HOURLY_LOAN_PREFIX = "[Préstamo por horas: ";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final LoanArticleService loanArticleService;
    private final AlertRepository alertRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public LoanArticleController(LoanArticleService loanArticleService, AlertRepository alertRepository, ArticleRepository articleRepository) {
        this.loanArticleService = Objects.requireNonNull(loanArticleService, "loanService must not be null");
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
        - Rango de horas: 'rangohoras:HH:mm:HH:mm' (ej: rangohoras:14:00:16:30)
        - Formato simplificado: 'rangohoras:HH:HH' (ej: rangohoras:14:16)
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
                        - rangohoras:14:00:16:30: Rango horario (formato completo)
                        - rangohoras:14:16: Rango horario (formato simplificado)
                        - tipo:Balon: Búsqueda por tipo de equipo
                        - horas:true: Préstamos por horas
                        """,
                            examples = {
                                    @ExampleObject(name = "Todos", value = ""),
                                    @ExampleObject(name = "Por ID", value = "LN-5f5b3b3b1f1b3b5f5b3b3b1f"),
                                    @ExampleObject(name = "Por usuario", value = "U-12345"),
                                    @ExampleObject(name = "Por estado", value = "Prestado"),
                                    @ExampleObject(name = "Por fechas", value = "fechas:2024-01-01:2024-02-01"),
                                    @ExampleObject(name = "Por rango horario completo", value = "rangohoras:14:00:16:30"),
                                    @ExampleObject(name = "Por rango horario simple", value = "rangohoras:14:16"),
                                    @ExampleObject(name = "Por tipo", value = "tipo:Balon"),
                                    @ExampleObject(name = "Por préstamos con horas", value = "horas:true")
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
                                    "loanDate": "2025-05-18",
                                    "devolutionDate": "2025-05-18",
                                    "articleIds": [101, 102],
                                    "startTime": "14:00",
                                    "endTime": "16:00",
                                    "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                                },
                                {
                                    "id": "LN-6a6c4c4c2a2c4c6a6c4c4c2a",
                                    "userId": "U-54321",
                                    "nameUser": "Ana Gómez",
                                    "loanStatus": "Devuelto",
                                    "loanDate": "2025-05-17",
                                    "devolutionDate": "2025-05-17",
                                    "articleIds": [103, 104],
                                    "startTime": "09:30", 
                                    "endTime": "11:00",
                                    "loanDescriptionType": "[Préstamo por horas: 09:30-11:00] Clase de tenis"
                                }
                            ]
                        }
                        """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Préstamos por rango de horas",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Búsqueda por rango horario",
                                                    value = """
                        {
                            "cantidad": 1,
                            "prestamos": [
                                {
                                    "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                    "userId": "U-12345",
                                    "nameUser": "Juan Pérez",
                                    "loanStatus": "Prestado",
                                    "loanDate": "2025-05-18",
                                    "devolutionDate": "2025-05-18",
                                    "articleIds": [101, 102],
                                    "startTime": "14:00",
                                    "endTime": "16:00",
                                    "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
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
                                    examples = {
                                            @ExampleObject(
                                                    name = "Error de formato de fechas",
                                                    value = """
                    {
                        "Error": "Error al obtener préstamos",
                        "Message": "Formato de fechas inválido, use: fechas:yyyy-MM-dd:yyyy-MM-dd"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Error de formato de horas",
                                                    value = """
                    {
                        "Error": "Error al obtener préstamos",
                        "Message": "Formato de horas inválido. Utilice rangohoras:HH:mm:HH:mm o rangohoras:HH:HH"
                    }
                    """
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<?> getLoans(@RequestParam(value = "q", required = false) String q) {
        try {
            List<LoanArticle> result;

            if (q == null || q.isBlank()) {
                // Obtener todos los préstamos
                result = loanArticleService.getLoans(null);
            } else if (q.startsWith("LN-") || ObjectId.isValid(q)) {
                // Búsqueda por ID - acepta tanto IDs con prefijo LN- como ObjectIds de MongoDB
                String searchId = q.startsWith("LN-") ? q.substring(3) : q;
                LoanArticle loanArticle = loanArticleService.getLoanById(searchId);
                result = (loanArticle != null) ? List.of(loanArticle) : List.of();
            } else if (q.startsWith("U-") || q.matches("\\d+")) {
                // Búsqueda por userId
                result = loanArticleService.getLoansByUserReport(q);
            } else if (q.startsWith("fechas:")) {
                // Búsqueda por rango de fechas: "fechas:2024-01-01:2024-02-01"
                String[] parts = q.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Formato de fechas inválido, use: fechas:yyyy-MM-dd:yyyy-MM-dd");
                }

                LocalDate startDate = LocalDate.parse(parts[1]);
                LocalDate endDate = LocalDate.parse(parts[2]);

                result = loanArticleService.getLoansByDateRangeAndStatus(startDate, endDate, null);
            } else if (q.startsWith("rangohoras:")) {
                // NUEVA FUNCIONALIDAD: Búsqueda por rango de horas: "rangohoras:14:00:17:30"
                String[] parts = q.split(":");
                if (parts.length != 3 && parts.length != 4) {
                    throw new IllegalArgumentException("Formato de horas inválido, use: rangohoras:HH:mm:HH:mm");
                }

                String startTimeStr, endTimeStr;
                if (parts.length == 3) {
                    // Formato simplificado: rangohoras:14:17
                    startTimeStr = parts[1] + ":00";
                    endTimeStr = parts[2] + ":00";
                } else {
                    // Formato completo: rangohoras:14:00:17:30
                    startTimeStr = parts[1] + ":" + parts[2];
                    endTimeStr = parts[3] + ":" + (parts.length > 4 ? parts[4] : "00");
                }

                try {
                    LocalTime startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
                    LocalTime endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
                    result = getLoansByTimeRange(startTime, endTime);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Formato de horas inválido. Utilice rangohoras:HH:mm:HH:mm o rangohoras:HH:HH", e);
                }
            } else if (q.startsWith("tipo:")) {
                // Búsqueda por tipo de equipo
                String tipoEquipo = q.substring("tipo:".length());
                result = getLoansByEquipmentType(tipoEquipo);
            } else if (q.startsWith("horas:")) {
                // Búsqueda de préstamos por horas
                boolean onlyHourly = Boolean.parseBoolean(q.substring("horas:".length()));
                result = getHourlyLoans(onlyHourly);
            } else {
                // Considerar la búsqueda por estado: "Prestado", "Devuelto", "Vencido"
                Set<String> estadosValidos = Set.of("Prestado", "Devuelto", "Vencido");

                if (estadosValidos.contains(q)) {
                    result = loanArticleService.getLoans(q);
                } else {
                    // Si no es un estado válido, buscar por nombre de usuario
                    result = loanArticleService.getLoans(null).stream()
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

    /**
     * Método para filtrar préstamos por rango de horas
     * Busca préstamos cuyo horario de inicio y fin se encuentre dentro del rango especificado,
     * o que al menos se solapen con el rango dado.
     *
     * @param startTime Hora de inicio del rango
     * @param endTime Hora de fin del rango
     * @return Lista de préstamos que coinciden con el rango horario
     */
    private List<LoanArticle> getLoansByTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Las horas de inicio y fin son obligatorias");
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
        }

        logger.info("Buscando préstamos en el rango horario: {} - {}",
                startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER));

        List<LoanArticle> allLoanArticles = loanArticleService.getLoans(null);

        return allLoanArticles.stream()
                .filter(loan -> {
                    // Para préstamos con campos startTime/endTime explícitos
                    if (loan.getStartTime() != null && loan.getEndTime() != null) {
                        // Verificar si hay solapamiento entre los rangos
                        return !loan.getStartTime().isAfter(endTime) && !loan.getEndTime().isBefore(startTime);
                    }

                    // Para préstamos antiguos que solo tienen la información en el loanDescriptionType
                    if (loan.getLoanDescriptionType() != null &&
                            loan.getLoanDescriptionType().startsWith(HOURLY_LOAN_PREFIX)) {
                        try {
                            String timeInfo = loan.getLoanDescriptionType()
                                    .substring(HOURLY_LOAN_PREFIX.length())
                                    .split("]")[0];
                            String[] times = timeInfo.split("-");
                            if (times.length == 2) {
                                LocalTime loanStartTime = LocalTime.parse(times[0], TIME_FORMATTER);
                                LocalTime loanEndTime = LocalTime.parse(times[1], TIME_FORMATTER);

                                // Verificar si hay solapamiento entre los rangos
                                return !loanStartTime.isAfter(endTime) && !loanEndTime.isBefore(startTime);
                            }
                        } catch (Exception e) {
                            logger.warn("No se pudo parsear el rango horario de la descripción: {}",
                                    loan.getLoanDescriptionType());
                        }
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }

    // Método para obtener préstamos por tipo de equipo
    private List<LoanArticle> getLoansByEquipmentType(String equipmentType) {
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
        return loanArticleService.getLoans(null).stream()
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
    private List<LoanArticle> getHourlyLoans(boolean onlyHourly) {
        List<LoanArticle> allLoanArticles = loanArticleService.getLoans(null);

        return allLoanArticles.stream()
                .filter(loan -> {
                    boolean isHourlyLoan = loan.getLoanDescriptionType() != null &&
                            loan.getLoanDescriptionType().startsWith(HOURLY_LOAN_PREFIX);
                    return onlyHourly ? isHourlyLoan : !isHourlyLoan;
                })
                .collect(Collectors.toList());
    }
    // Filtrar préstamos por duración (cortos/largos)
    private List<LoanArticle> getLoansByDuration(boolean shortLoans) {
        List<LoanArticle> allLoanArticles = loanArticleService.getLoans(null);
        final int DURATION_THRESHOLD_MINUTES = 120; // 2 horas como umbral

        return allLoanArticles.stream()
                .filter(loan -> {
                    // Si no tiene horas definidas, usar la descripción (para préstamos antiguos)
                    if (loan.getStartTime() == null || loan.getEndTime() == null) {
                        return false; // O manejar según la lógica de migración
                    }

                    Duration duration = Duration.between(loan.getStartTime(), loan.getEndTime());
                    return shortLoans ?
                            duration.toMinutes() <= DURATION_THRESHOLD_MINUTES :
                            duration.toMinutes() > DURATION_THRESHOLD_MINUTES;
                })
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(
            summary = "Crear préstamo",
            description = "Registra un nuevo préstamo de artículos deportivos por horas. Todos los préstamos son por horas y se devuelven el mismo día.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del préstamo a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoanArticle.class),
                            examples = @ExampleObject(
                                    value = """
                {
                    "articleIds": [101, 102],
                    "nameUser": "Juan Cely",
                    "userId": "U-12345",
                    "userRole": "Estudiante",
                    "loanDescriptionType": "Clase deportiva",
                    "loanDate": "2025-05-18",
                    "loanStatus": "Prestado",
                    "equipmentStatus": "En buen estado"
                }
                """
                            )
                    )
            ),
            parameters = {
                    @Parameter(
                            name = "startTime",
                            description = "Hora de inicio (HH:MM) del préstamo",
                            required = true,
                            example = "14:00"
                    ),
                    @Parameter(
                            name = "endTime",
                            description = "Hora de fin (HH:MM) del préstamo",
                            required = true,
                            example = "16:00"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Préstamo creado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                        "articleIds": [101, 102],
                        "nameUser": "Juan Cely",
                        "userId": "U-12345",
                        "userRole": "Estudiante",
                        "loanStatus": "Prestado",
                        "loanDate": "2025-05-18",
                        "devolutionDate": "2025-05-18",
                        "startTime": "14:00",
                        "endTime": "16:00",
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
                            "Message": "Los siguientes artículos no están disponibles o están dañados: [101]" 
                        }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Formato de hora inválido",
                                                    value = """ 
                        { 
                            "Error": "Error al crear préstamo",
                            "Message": "Formato de hora inválido. Use HH:MM" 
                        }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Horas incorrectas",
                                                    value = """ 
                        { 
                            "Error": "Error al crear préstamo",
                            "Message": "La hora de inicio no puede ser posterior a la hora de fin" 
                        }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Artículos requeridos",
                                                    value = """ 
                        { 
                            "Error": "Error al crear préstamo",
                            "Message": "Debe incluir al menos un artículo" 
                        }"""
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Error al crear préstamo",
                        "Message": "Error interno del servidor"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Object> save(
            @Valid @RequestBody LoanArticle loanArticle,
            @RequestParam(value = "startTime", required = true) String startTime,
            @RequestParam(value = "endTime", required = true) String endTime) {

        logger.info("Creando nuevo préstamo para usuario: {}", loanArticle.getUserId());
        try {
            // Validar que los artículos estén disponibles y no dañados
            validateArticlesAvailable(loanArticle.getArticleIds());

            // Procesar las horas del préstamo (ahora obligatorias)
            LocalTime start;
            LocalTime end;

            try {
                start = LocalTime.parse(startTime, TIME_FORMATTER);
                end = LocalTime.parse(endTime, TIME_FORMATTER);
            } catch (Exception e) {
                throw new IllegalArgumentException("Formato de hora inválido. Use HH:MM", e);
            }

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
            }

            // Si la fecha de préstamo no se especificó, usar hoy
            if (loanArticle.getLoanDate() == null) {
                loanArticle.setLoanDate(LocalDate.now());
            }

            // Para préstamos por horas, la fecha de devolución es la misma que la de préstamo
            loanArticle.setDevolutionDate(loanArticle.getLoanDate());

            // Establecer los campos de horas
            loanArticle.setStartTime(start);
            loanArticle.setEndTime(end);

            // Mantener también la descripción con formato de hora para mostrar en UI
            String originalDescription = loanArticle.getLoanDescriptionType() != null ?
                    loanArticle.getLoanDescriptionType() : "";

            // Si la descripción no tiene el formato con horas, agregarlo
            if (!originalDescription.contains(HOURLY_LOAN_PREFIX)) {
                loanArticle.setLoanDescriptionType(
                        HOURLY_LOAN_PREFIX + startTime + "-" + endTime + "] " + originalDescription);
            }

            LoanArticle createdLoanArticle = loanArticleService.createLoan(loanArticle);
            logger.info("Préstamo creado con ID: {}", createdLoanArticle.getId());
            return new ResponseEntity<>(createdLoanArticle, HttpStatus.CREATED);

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

    private void handleHourlyLoan(LoanArticle loanArticle, String startTimeStr, String endTimeStr) {
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
        if (loanArticle.getLoanDate() == null) {
            loanArticle.setLoanDate(LocalDate.now());
        }

        // Para préstamos por horas, la fecha de devolución es la misma que la de préstamo
        loanArticle.setDevolutionDate(loanArticle.getLoanDate());

        // Guardar información de horas en la descripción del préstamo
        String originalDescription = loanArticle.getLoanDescriptionType() != null ? loanArticle.getLoanDescriptionType() : "";
        if (!originalDescription.startsWith(HOURLY_LOAN_PREFIX)) {
            loanArticle.setLoanDescriptionType(HOURLY_LOAN_PREFIX + startTimeStr + "-" + endTimeStr + "] " + originalDescription);
        }
    }

    @GetMapping("/reports")
    @Operation(
            summary = "Generar reportes de préstamos",
            description = """
        Genera reportes especializados de préstamos con diferentes criterios de filtrado:
        - student: Préstamos de un estudiante específico
        - equipment: Préstamos que incluyen un tipo específico de equipamiento
        - status: Préstamos filtrados por estado (Prestado, Devuelto, Vencido)
        - hourly: Préstamos por horas (todos los préstamos son por horas)
        - daterange: Préstamos dentro de un rango de fechas específico
        
        Para cada tipo de reporte se requieren diferentes parámetros.
        """,
            parameters = {
                    @Parameter(
                            name = "type",
                            description = "Tipo de reporte a generar",
                            required = true,
                            schema = @Schema(allowableValues = {"student", "equipment", "status", "hourly", "daterange"}),
                            examples = {
                                    @ExampleObject(name = "Por estudiante", value = "student"),
                                    @ExampleObject(name = "Por equipo", value = "equipment"),
                                    @ExampleObject(name = "Por estado", value = "status"),
                                    @ExampleObject(name = "Por horas", value = "hourly"),
                                    @ExampleObject(name = "Por rango de fechas", value = "daterange")
                            }
                    ),
                    @Parameter(
                            name = "value",
                            description = """
                        Valor para filtrar el reporte:
                        - Para 'student': ID o nombre del estudiante (ej: U-12345)
                        - Para 'equipment': Tipo de equipo (ej: Balon, Raqueta)
                        - Para 'status': Estado del préstamo (Prestado, Devuelto, Vencido)
                        - Para 'hourly': 'true' para préstamos por horas
                        """,
                            examples = {
                                    @ExampleObject(name = "ID de estudiante", value = "U-54321"),
                                    @ExampleObject(name = "Tipo de equipo", value = "Balon"),
                                    @ExampleObject(name = "Estado", value = "Prestado"),
                                    @ExampleObject(name = "Por horas", value = "true")
                            }
                    ),
                    @Parameter(
                            name = "startDate",
                            description = "Fecha inicial para reportes por rango de fechas (formato yyyy-MM-dd)",
                            example = "2025-05-01"
                    ),
                    @Parameter(
                            name = "endDate",
                            description = "Fecha final para reportes por rango de fechas (formato yyyy-MM-dd)",
                            example = "2025-05-30"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte generado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Reporte por tipo de equipo",
                                                    value = """
                    {
                        "reportType": "equipment",
                        "reportValue": "Balon",
                        "totalItems": 2,
                        "data": [
                            {
                                "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                "userId": "U-12345",
                                "nameUser": "Juan-cely-l",
                                "loanDate": "2025-05-18",
                                "devolutionDate": "2025-05-18",
                                "loanStatus": "Prestado",
                                "startTime": "14:00",
                                "endTime": "16:00",
                                "articleIds": [101, 102],
                                "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                            },
                            {
                                "id": "LN-6a6c4c4c2a2c4c6a6c4c4c2a",
                                "userId": "U-54321",
                                "nameUser": "Ana Gómez",
                                "loanDate": "2025-05-16",
                                "devolutionDate": "2025-05-16",
                                "loanStatus": "Devuelto",
                                "startTime": "09:00",
                                "endTime": "11:00",
                                "articleIds": [103, 101],
                                "loanDescriptionType": "[Préstamo por horas: 09:00-11:00] Entrenamiento"
                            }
                        ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Reporte por estudiante",
                                                    value = """
                    {
                        "reportType": "student",
                        "reportValue": "U-12345",
                        "totalItems": 1,
                        "data": [
                            {
                                "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                "userId": "U-12345",
                                "nameUser": "Juan-cely-l",
                                "loanDate": "2025-05-18",
                                "devolutionDate": "2025-05-18",
                                "loanStatus": "Prestado",
                                "startTime": "14:00",
                                "endTime": "16:00",
                                "articleIds": [101, 102],
                                "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                            }
                        ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Reporte por rango de fechas",
                                                    value = """
                    {
                        "reportType": "daterange",
                        "startDate": "2025-05-01",
                        "endDate": "2025-05-30",
                        "totalItems": 3,
                        "data": [
                            {
                                "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                "userId": "U-12345",
                                "nameUser": "Juan-cely-l",
                                "loanDate": "2025-05-18",
                                "loanStatus": "Prestado"
                            },
                            {
                                "id": "LN-6a6c4c4c2a2c4c6a6c4c4c2a",
                                "userId": "U-54321",
                                "nameUser": "Ana Gómez",
                                "loanDate": "2025-05-16",
                                "loanStatus": "Devuelto"
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
                                    examples = {
                                            @ExampleObject(
                                                    name = "Tipo inválido",
                                                    value = """
                    {
                        "Error": "Error al generar reporte",
                        "Message": "Tipo de reporte inválido. Use: student, equipment, status, hourly, o dateRange"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Falta valor",
                                                    value = """
                    {
                        "Error": "Error al generar reporte",
                        "Message": "Debe proporcionar un ID de estudiante"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Falta fechas",
                                                    value = """
                    {
                        "Error": "Error al generar reporte",
                        "Message": "Debe proporcionar fechas de inicio y fin"
                    }
                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                {
                    "Error": "Error al generar reporte",
                    "Message": "Error interno: Error en la consulta de la base de datos"
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
            List<LoanArticle> reportData = Collections.emptyList();

            report.put("reportType", reportType);

            switch (reportType.toLowerCase()) {
                case "student":
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException("Debe proporcionar un ID de estudiante");
                    }
                    reportData = loanArticleService.getLoansByUserReport(value);
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
                    reportData = loanArticleService.getLoans(value);
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
                    reportData = loanArticleService.getLoansByDateRangeAndStatus(startDate, endDate, null);
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
            description = """
        Actualiza un préstamo existente. Permite realizar diferentes tipos de actualizaciones:
        
        1. Devolución completa: Marcar un préstamo como devuelto usando el campo "devolver": true
        2. Actualizar horarios: Cambiar las horas de inicio y fin usando los parámetros startTime/endTime
        3. Actualizar estados de artículos individuales: Usando "articulos": {"101": "Dañado", "102": "En buen estado"}
        4. Modificar campos generales: Como loanStatus, loanDescriptionType, etc.
        
        Las actualizaciones se pueden combinar en una sola petición según se necesite.
        """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID único del préstamo que se va a actualizar",
                            example = "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                            required = true
                    ),
                    @Parameter(
                            name = "startTime",
                            description = "Nueva hora de inicio (HH:MM) - opcional",
                            example = "15:00"
                    ),
                    @Parameter(
                            name = "endTime",
                            description = "Nueva hora de fin (HH:MM) - opcional",
                            example = "17:30"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del préstamo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Actualización general",
                                            value = """
                    {
                        "equipmentStatus": "Dañado",
                        "loanStatus": "Prestado",
                        "loanDescriptionType": "Clase de baloncesto actualizada"
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Devolución completa",
                                            value = """
                    {
                        "devolver": true
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Actualización de estados de artículos",
                                            value = """
                    {
                        "articulos": {
                            "101": "Dañado",
                            "102": "En buen estado"
                        }
                    }
                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Préstamo actualizado exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Actualización general",
                                                    value = """
                    {
                        "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                        "userId": "U-12345",
                        "nameUser": "Juan-cely-l",
                        "equipmentStatus": "Dañado",
                        "loanStatus": "Prestado",
                        "articleIds": [101, 102],
                        "loanDate": "2025-05-18",
                        "devolutionDate": "2025-05-18",
                        "startTime": "15:00",
                        "endTime": "17:30",
                        "loanDescriptionType": "[Préstamo por horas: 15:00-17:30] Clase de baloncesto actualizada"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Préstamo devuelto",
                                                    value = """
                    {
                        "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                        "userId": "U-12345",
                        "nameUser": "Juan-cely-l",
                        "equipmentStatus": "En buen estado",
                        "loanStatus": "Devuelto",
                        "articleIds": [101, 102],
                        "loanDate": "2025-05-18",
                        "devolutionDate": "2025-05-18",
                        "startTime": "14:00",
                        "endTime": "16:00",
                        "loanDescriptionType": "[Préstamo por horas: 14:00-16:00] Clase deportiva"
                    }
                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error en actualización",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Préstamo ya devuelto",
                                                    value = """
                    {
                        "Error": "Error al actualizar préstamo",
                        "Message": "No se puede modificar un préstamo ya devuelto"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Horas inválidas",
                                                    value = """
                    {
                        "Error": "Error al actualizar préstamo",
                        "Message": "La hora de inicio no puede ser posterior a la hora de fin"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Formato de hora incorrecto",
                                                    value = """
                    {
                        "Error": "Error al actualizar préstamo",
                        "Message": "Formato de hora inválido. Use HH:MM"
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "ID de artículo inválido",
                                                    value = """
                    {
                        "Error": "Error al actualizar préstamo",
                        "Message": "ID de artículo inválido: ABC"
                    }
                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Préstamo no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                {
                    "Error": "Error al actualizar préstamo",
                    "Message": "No se encontró el préstamo con ID: LN-5f5b3b3b1f1b3b5f5b3b3b1f"
                }
                """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                {
                    "Error": "Error al actualizar préstamo",
                    "Message": "Error interno del servidor"
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
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {

        logger.info("Actualizando préstamo con ID: {}, campos: {}", id, updates.keySet());
        try {
            // Verificar si es una devolución completa
            if (updates.containsKey("devolver") && Boolean.TRUE.equals(updates.get("devolver"))) {
                loanArticleService.devolverLoan(id);
                LoanArticle updatedLoanArticle = loanArticleService.getLoanById(id);
                return new ResponseEntity<>(updatedLoanArticle, HttpStatus.OK);
            }

            // Actualizar información de horas si se proporciona
            if (startTime != null || endTime != null) {
                LoanArticle loanArticle = loanArticleService.getLoanById(id);

                // Obtener valores actuales de hora
                LocalTime currentStart = loanArticle.getStartTime();
                LocalTime currentEnd = loanArticle.getEndTime();

                // Si los campos son nulos (préstamos antiguos), intentar obtenerlos de la descripción
                if ((currentStart == null || currentEnd == null) &&
                        loanArticle.getLoanDescriptionType() != null &&
                        loanArticle.getLoanDescriptionType().startsWith(HOURLY_LOAN_PREFIX)) {

                    String timeInfo = loanArticle.getLoanDescriptionType()
                            .substring(HOURLY_LOAN_PREFIX.length())
                            .split("]")[0];
                    String[] times = timeInfo.split("-");
                    if (times.length == 2) {
                        try {
                            if (currentStart == null) {
                                currentStart = LocalTime.parse(times[0], TIME_FORMATTER);
                            }
                            if (currentEnd == null) {
                                currentEnd = LocalTime.parse(times[1], TIME_FORMATTER);
                            }
                        } catch (Exception e) {
                            logger.warn("No se pudo parsear las horas de la descripción: {}", timeInfo);
                        }
                    }
                }

                // Actualizar con los nuevos valores o mantener los actuales
                String newStartTimeStr = startTime != null ? startTime :
                        (currentStart != null ? currentStart.format(TIME_FORMATTER) : null);
                String newEndTimeStr = endTime != null ? endTime :
                        (currentEnd != null ? currentEnd.format(TIME_FORMATTER) : null);

                if (newStartTimeStr != null && newEndTimeStr != null) {
                    try {
                        LocalTime start = LocalTime.parse(newStartTimeStr, TIME_FORMATTER);
                        LocalTime end = LocalTime.parse(newEndTimeStr, TIME_FORMATTER);

                        if (start.isAfter(end)) {
                            throw new IllegalArgumentException(
                                    "La hora de inicio no puede ser posterior a la hora de fin");
                        }

                        // Actualizar campos de hora
                        updates.put("startTime", start);
                        updates.put("endTime", end);

                        // Actualizar también la descripción para mantener consistencia
                        // Extraer descripción original sin las horas
                        String originalDesc = loanArticle.getLoanDescriptionType();
                        if (originalDesc != null && originalDesc.startsWith(HOURLY_LOAN_PREFIX)) {
                            int endBracketPos = originalDesc.indexOf(']');
                            if (endBracketPos >= 0 && endBracketPos + 1 < originalDesc.length()) {
                                originalDesc = originalDesc.substring(endBracketPos + 1).trim();
                            } else {
                                originalDesc = "";
                            }
                        }

                        // Actualizar descripción con nuevas horas
                        updates.put("loanDescriptionType",
                                HOURLY_LOAN_PREFIX + newStartTimeStr + "-" + newEndTimeStr + "] " + originalDesc);

                    } catch (Exception e) {
                        throw new IllegalArgumentException("Formato de hora inválido. Use HH:MM", e);
                    }
                }
            }

            // Manejar la actualización de artículos individuales
            Map<String, String> articulosUpdate = null;
            if (updates.containsKey("articulos")) {
                articulosUpdate = extractArticulosMap(updates.get("articulos"));
                loanArticleService.updateArticlesStatus(id, articulosUpdate);
                updates.remove("articulos");
            }

            // Actualizar campos generales del préstamo
            if (!updates.isEmpty()) {
                loanArticleService.updateLoan(id, updates);
            }

            // Devolver el préstamo actualizado
            LoanArticle updatedLoanArticle = loanArticleService.getLoanById(id);
            return new ResponseEntity<>(updatedLoanArticle, HttpStatus.OK);

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
            description = """
        Elimina un préstamo del sistema.
        
        Restricciones:
        - Solo se pueden eliminar préstamos en estado "Prestado"
        - No se pueden eliminar préstamos ya devueltos o vencidos
        - Al eliminar un préstamo, los artículos asociados vuelven a estar disponibles
        
        Esta operación es permanente y no se puede deshacer.
        """,
            parameters = @Parameter(
                    name = "id",
                    description = "ID único del préstamo (formato LN-xxxx o identificador MongoDB)",
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
                            description = "No se puede eliminar el préstamo debido a restricciones",
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
                                            ),
                                            @ExampleObject(
                                                    name = "ID inválido",
                                                    value = """ 
                        { 
                            "Error": "Error al eliminar préstamo",
                            "Message": "Formato de ID inválido" 
                        }"""
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Préstamo no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Error al eliminar préstamo",
                        "Message": "No se encontró el préstamo con ID: LN-5f5b3b3b1f1b3b5f5b3b3b1f"
                    }
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Error al eliminar préstamo",
                        "Message": "Error interno del servidor"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Object> delete(@PathVariable String id) {
        logger.info("Solicitando eliminar préstamo con ID: {}", id);
        try {
            loanArticleService.deleteLoanById(id);
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
            description = """
        Recupera alertas relacionadas con préstamos deportivos. Permite:
        
        - Filtrar por ID de usuario para ver solo alertas de un usuario específico
        - Limitar alertas a un período reciente (últimos N días)
        - Paginar resultados para mejor rendimiento
        
        Las alertas incluyen notificaciones sobre préstamos vencidos, recordatorios de devolución,
        y cambios de estado en préstamos. Los resultados se ordenan por fecha más reciente primero.
        """,
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = """
                        ID del usuario para filtrar alertas (opcional).
                        Puede ser el ID completo (ej: U-12345) o parte del mismo.
                        También busca coincidencias en el contenido del mensaje.
                        """,
                            example = "Juan-cely-l",
                            required = false
                    ),
                    @Parameter(
                            name = "days",
                            description = """
                        Filtrar alertas de los últimos N días (opcional).
                        Si no se especifica, se devuelven todas las alertas disponibles.
                        """,
                            example = "7",
                            schema = @Schema(type = "integer", minimum = "1")
                    ),
                    @Parameter(
                            name = "page",
                            description = """
                        Número de página para paginación (empieza en 0).
                        Con page=0 se obtiene la primera página de resultados.
                        """,
                            example = "0",
                            schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")
                    ),
                    @Parameter(
                            name = "size",
                            description = """
                        Tamaño de página: número de alertas por página.
                        Valores recomendados: 5-20 para mejor rendimiento.
                        """,
                            example = "10",
                            schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "100")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Alertas obtenidas exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Alertas para usuario específico",
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
                            "timestamp": "2025-05-18T14:30:45"
                        },
                        {
                            "id": "65a1f3e8d4e8b10c9c8b4568",
                            "description": "Juan-cely-l",
                            "message": "Recordatorio: Devolución pendiente para hoy (2025-05-18) de Balones de fútbol",
                            "timestamp": "2025-05-18T09:00:00"
                        }
                    ]
                }
                """
                                            ),
                                            @ExampleObject(
                                                    name = "Alertas por días",
                                                    value = """
                {
                    "totalAlertas": 3,
                    "page": 0,
                    "size": 10,
                    "alertas": [
                        {
                            "id": "65a1f3e8d4e8b10c9c8b4569",
                            "description": "Sistema",
                            "message": "Préstamo actualizado: LN-5f5b3b3b1f1b3b5f5b3b3b1f - Horario modificado a 15:00-17:30",
                            "timestamp": "2025-05-18T22:15:30"
                        },
                        {
                            "id": "65a1f3e8d4e8b10c9c8b4570",
                            "description": "U-54321",
                            "message": "Artículo reportado como dañado en devolución: Balón #101",
                            "timestamp": "2025-05-17T16:45:12"
                        },
                        {
                            "id": "65a1f3e8d4e8b10c9c8b4571",
                            "description": "U-12345",
                            "message": "Nuevo préstamo creado: LN-7d8e9f0a1b2c3d4e5f6a7b8c - Raquetas",
                            "timestamp": "2025-05-16T10:20:05"
                        }
                    ]
                }
                """
                                            ),
                                            @ExampleObject(
                                                    name = "Sin alertas",
                                                    value = """
                {
                    "totalAlertas": 0,
                    "page": 0,
                    "size": 10,
                    "alertas": []
                }
                """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros de solicitud inválidos",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                {
                    "Error": "Error al obtener alertas",
                    "Message": "Valor negativo no permitido para el parámetro 'days'"
                }
                """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
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
            // Validación básica de parámetros
            if (days != null && days <= 0) {
                throw new IllegalArgumentException("Valor negativo no permitido para el parámetro 'days'");
            }

            if (page < 0 || size <= 0 || size > 100) {
                throw new IllegalArgumentException("Parámetros de paginación inválidos");
            }

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

        } catch (IllegalArgumentException e) {
            logger.error("Parámetros inválidos: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al obtener alertas");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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