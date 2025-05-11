package edu.eci.cvds.proyect.coliseum.persistency.controller;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("loan")
@Tag(
        name = "Préstamos",
        description = "Gestión integral de préstamos de artículos deportivos (balones, raquetas, lazos, etc.)"
)
@Validated
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private static final String ERROR_KEY = "error";
    private static final String LOANS_KEY = "loans";
    private static final String LOAN_KEY = "loan";
    private static final String MESSAGE_KEY = "message";
    private static final String UPDATED_FIELDS_KEY = "updated_fields";

    private final LoanService loanService;

    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = Objects.requireNonNull(loanService, "loanService must not be null");
    }

    @PostMapping
    @Operation(
            summary = "Crear préstamo",
            description = "Registra un nuevo préstamo de artículos deportivos",
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
                        "LoanDescriptionType": "Préstamo para torneo ",
                        "loanDate": "2024-03-15",
                        "devolutionDate": "2024-03-22",
                        "loanStatus": "Prestado",
                        "equipmentStatus": "En buen estado"
                    }
                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Préstamo creado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "loan": {
                                "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                "articleIds": [101, 102],
                                "nameUser": "Juan Pérez",
                                "loanStatus": "Prestado",
                                "loanDate": "2024-03-15",
                                "devolutionDate": "2024-03-22"
                            }
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
                                "error": "Los siguientes artículos no están disponibles: [101]" 
                            }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Fecha inválida",
                                                    value = """ 
                            { 
                                "error": "La fecha de devolución no puede ser en el pasado" 
                            }"""
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<Map<String, Object>> createLoan(
            @Parameter(description = "Datos del préstamo a crear", required = true)
            @Valid @RequestBody Loan loan) {
        logger.info("Creando nuevo préstamo para usuario: {}", loan.getUserId());
        try {
            Loan createdLoan = loanService.createLoan(loan);
            logger.info("Préstamo creado con ID: {}", createdLoan.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap(LOAN_KEY, createdLoan));
        } catch (LoanException | ArticleException e) {
            logger.error("Error al crear préstamo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap(ERROR_KEY, e.getMessage()));
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
                            responseCode = "204",
                            description = "Préstamo eliminado exitosamente"
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
                                "error": "No se puede eliminar un préstamo devuelto" 
                            }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Préstamo vencido",
                                                    value = """ 
                            { 
                                "error": "No se puede eliminar un préstamo vencido" 
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
                            "error": "Préstamo no encontrado con ID: LN-invalido" 
                        }"""
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> deleteLoan(
            @Parameter(description = "ID del préstamo a eliminar", required = true)
            @PathVariable String id) {
        logger.info("Solicitando eliminar préstamo con ID: {}", id);
        try {
            validateId(id);
            loanService.deleteLoanById(id);
            logger.info("Préstamo eliminado exitosamente: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (LoanException e) {
            logger.error("Error al eliminar préstamo {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap(ERROR_KEY, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("ID de préstamo inválido: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap(ERROR_KEY, e.getMessage()));
        }
    }

    // Endpoint GET unificado
    @GetMapping
    @Operation(
            summary = "Obtener préstamos",
            description = "Recupera préstamos con múltiples criterios de búsqueda",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID del préstamo"
                    ),
                    @Parameter(
                            name = "userId",
                            description = "ID del usuario"
                    ),
                    @Parameter(
                            name = "status",
                            description = "Estado del préstamo",
                            schema = @Schema(allowableValues = {"Prestado", "Vencido", "Devuelto"})
                    ),
                    @Parameter(
                            name = "startDate",
                            description = "Fecha de inicio (AAAA-MM-DD)"
                    ),
                    @Parameter(
                            name = "endDate",
                            description = "Fecha de fin (AAAA-MM-DD)"
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
                                                    name = "Por usuario",
                                                    value = """
                            {
                                "loans": [
                                    {
                                        "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                        "userId": "U-12345",
                                        "loanStatus": "Prestado",
                                        "articleIds": [101, 102]
                                    }
                                ]
                            }
                            """
                                            ),
                                            @ExampleObject(
                                                    name = "Por rango de fechas",
                                                    value = """
                            {
                                "loans": [
                                    {
                                        "id": "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                                        "loanDate": "2024-03-15",
                                        "devolutionDate": "2024-03-22",
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
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "error": "Las fechas de inicio y fin son requeridas"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Map<String, Object>> getLoans(
            @Parameter(description = "ID del préstamo") @RequestParam(required = false) String id,
            @Parameter(description = "ID del usuario") @RequestParam(required = false) String userId,
            @Parameter(description = "Estado del préstamo") @RequestParam(required = false) String status,
            @Parameter(description = "Fecha de inicio") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Fecha de fin") @RequestParam(required = false) LocalDate endDate) {

        logger.info("Buscando préstamos con filtros: id={}, userId={}, status={}, startDate={}, endDate={}",
                id, userId, status, startDate, endDate);

        try {
            if (id != null && !id.trim().isEmpty()) {
                validateId(id);
                return handleGetById(id);
            }

            if (userId != null && !userId.trim().isEmpty()) {
                return handleGetByUser(userId);
            }

            if (startDate != null && endDate != null) {
                validateDateRange(startDate, endDate);
                return handleGetByDateRange(startDate, endDate, status);
            }

            return handleGetAll(status);
        } catch (LoanException e) {
            logger.error("Error al buscar préstamos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap(ERROR_KEY, e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap(ERROR_KEY, e.getMessage()));
        }
    }

    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del préstamo no puede estar vacío");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private ResponseEntity<Map<String, Object>> handleGetById(String id) {
        Loan loan = loanService.getLoanById(id);
        logger.info("Préstamo encontrado con ID: {}", id);
        return ResponseEntity.ok(Collections.singletonMap(LOAN_KEY, loan));
    }

    private ResponseEntity<Map<String, Object>> handleGetByUser(String userId) {
        List<Loan> loans = loanService.getLoansByUserReport(userId);
        logger.info("Encontrados {} préstamos para usuario: {}", loans.size(), userId);
        return ResponseEntity.ok(Collections.singletonMap(LOANS_KEY, loans));
    }

    private ResponseEntity<Map<String, Object>> handleGetByDateRange(LocalDate start, LocalDate end, String status) {
        List<Loan> loans = loanService.getLoansByDateRangeAndStatus(start, end, status);
        logger.info("Encontrados {} préstamos en rango de fechas: {} - {}, estado: {}",
                loans.size(), start, end, status);
        return ResponseEntity.ok(Collections.singletonMap(LOANS_KEY, loans));
    }

    private ResponseEntity<Map<String, Object>> handleGetAll(String status) {
        List<Loan> loans = loanService.getLoans(status);
        logger.info("Recuperados {} préstamos con estado: {}", loans.size(), status);
        return ResponseEntity.ok(Collections.singletonMap(LOANS_KEY, loans));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Actualizar préstamo",
            description = "Actualización parcial de préstamos y estados de artículos",
            parameters = @Parameter(
                    name = "id",
                    description = "ID del préstamo",
                    example = "LN-5f5b3b3b1f1b3b5f5b3b3b1f",
                    required = true
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Campos a actualizar",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Devolución completa",
                                            value = """
                        {
                            "devolver": true
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Actualización parcial",
                                            value = """
                        {
                            "equipmentStatus": "Dañado",
                            "articulos": {
                                "101": "RequiereMantenimiento"
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
                            description = "Actualización exitosa",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "message": "Préstamo actualizado",
                            "updated_fields": ["equipmentStatus", "articulos"]
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
                            "error": "Artículo 103 no pertenece al préstamo"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Map<String, Object>> updateLoan(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {

        logger.info("Actualizando préstamo con ID: {}, campos: {}", id, updates.keySet());
        try {
            validateId(id);
            validateUpdatePayload(updates);

            // Manejar devolución primero
            if (updates.containsKey("devolver") && Boolean.TRUE.equals(updates.get("devolver"))) {
                return handleDevolver(id);
            }

            // Actualización de artículos individuales
            Map<String, String> articulosUpdate = null;
            if (updates.containsKey("articulos")) {
                articulosUpdate = extractArticulosMap(updates.get("articulos"));
                loanService.updateArticlesStatus(id, articulosUpdate);
                updates.remove("articulos");
            }

            // Procesar demás actualizaciones
            if (!updates.isEmpty()) {
                loanService.updateLoan(id, updates);
            }

            Map<String, Object> response = new HashMap<>();
            response.put(MESSAGE_KEY, "Préstamo actualizado");

            // Preparar lista de campos actualizados
            if (articulosUpdate != null) {
                Map<String, Object> updatedMap = new HashMap<>(updates);
                updatedMap.put("articulos", articulosUpdate.keySet());
                response.put(UPDATED_FIELDS_KEY, updatedMap.keySet());
            } else {
                response.put(UPDATED_FIELDS_KEY, updates.keySet());
            }

            logger.info("Préstamo {} actualizado exitosamente", id);
            return ResponseEntity.ok().body(response);

        } catch (LoanException | IllegalArgumentException e) {
            logger.error("Error al actualizar préstamo {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap(ERROR_KEY, e.getMessage()));
        } catch (ClassCastException e) {
            logger.error("Error de formato en la actualización del préstamo {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap(ERROR_KEY, "Formato inválido en los datos de actualización: " + e.getMessage()));
        }
    }

    private void validateUpdatePayload(Map<String, Object> updates) {
        Objects.requireNonNull(updates, "El cuerpo de la actualización no puede ser nulo");

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("Al menos un campo debe ser actualizado");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractArticulosMap(Object articulosObj) {
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

    private ResponseEntity<Map<String, Object>> handleDevolver(String id) {
        loanService.devolverLoan(id);
        logger.info("Préstamo {} devuelto exitosamente", id);

        Map<String, Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, "Préstamo devuelto");
        response.put("details", "Todos los artículos actualizados según estado del equipo");

        return ResponseEntity.ok().body(response);
    }
}