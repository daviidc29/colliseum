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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("loan")
@Tag(
    name = "Préstamos", 
    description = "Gestión integral de préstamos de artículos deportivos (balones, raquetas, lazos, etc.)"
)
public class LoanController {

    @Autowired
    private LoanService loanService;

    

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

    public ResponseEntity<?> createLoan(
        @Parameter(description = "Datos del préstamo a crear", required = true)
        @Valid @RequestBody Loan loan) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("loan", loanService.createLoan(loan)));
        } catch (LoanException | ArticleException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
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
        try {
            loanService.deleteLoanById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
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
    public ResponseEntity<?> getLoans(
            @Parameter(description = "ID del préstamo") @RequestParam(required = false) String id,
            @Parameter(description = "ID del usuario") @RequestParam(required = false) String userId,
            @Parameter(description = "Estado del préstamo") @RequestParam(required = false) String status,
            @Parameter(description = "Fecha de inicio") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Fecha de fin") @RequestParam(required = false) LocalDate endDate) {

        try {
            if (id != null) {
                return handleGetById(id);
            }
            if (userId != null) {
                return handleGetByUser(userId);
            }
            if (startDate != null && endDate != null) {
                return handleGetByDateRange(startDate, endDate, status);
            }
            return handleGetAll(status);

        } catch (LoanException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    private ResponseEntity<?> handleGetById(String id) {
        return ResponseEntity.ok(Collections.singletonMap("loan", loanService.getLoanById(id)));
    }

    private ResponseEntity<?> handleGetByUser(String userId) {
        List<Loan> loans = loanService.getLoansByUserReport(userId);
        return ResponseEntity.ok(Collections.singletonMap("loans", loans));
    }

    private ResponseEntity<?> handleGetByDateRange(LocalDate start, LocalDate end, String status) {
        List<Loan> loans = loanService.getLoansByDateRangeAndStatus(start, end, status);
        return ResponseEntity.ok(Collections.singletonMap("loans", loans));
    }

    private ResponseEntity<?> handleGetAll(String status) {
        return ResponseEntity.ok(Collections.singletonMap("loans", loanService.getLoans(status)));
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
    public ResponseEntity<?> updateLoan(
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {

        try {
            // Manejar devolución primero
            if (updates.containsKey("devolver") && Boolean.TRUE.equals(updates.get("devolver"))) {
                return handleDevolver(id);
            }

            // Actualización de artículos individuales
            if (updates.containsKey("articulos")) {
                Map<String, String> articulosUpdate = (Map<String, String>) updates.get("articulos");
                loanService.updateArticlesStatus(id, articulosUpdate);
                updates.remove("articulos");
            }

            // Procesar demás actualizaciones
            loanService.updateLoan(id, updates);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Préstamo actualizado",
                    "updated_fields", updates.keySet()
            ));

        } catch (LoanException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    private ResponseEntity<?> handleDevolver(String id) {
        loanService.devolverLoan(id);
        return ResponseEntity.ok().body(Map.of(
                "message", "Préstamo devuelto",
                "details", "Todos los artículos actualizados según estado del equipo"
        ));
    }






}
