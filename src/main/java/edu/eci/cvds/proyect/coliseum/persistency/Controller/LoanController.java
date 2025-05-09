package edu.eci.cvds.proyect.coliseum.persistency.Controller;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Préstamos", description = "Operaciones relacionadas con la gestión de préstamos de artículos")
public class LoanController {

    @Autowired
    private LoanService loanService;



    @PostMapping
    @Operation(summary = "Crear un nuevo préstamo", description = "Crea un préstamo nuevo con los datos proporcionados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Préstamo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error al crear el préstamo")
    })
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
    @Operation(summary = "Eliminar préstamo", description = "Elimina un préstamo existente por su ID.")
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
    @Operation(summary = "Obtener préstamos", description = "Recupera préstamos con múltiples criterios de búsqueda")
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

    // Endpoint PATCH unificado
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar préstamo", description = "Actualiza préstamo y/o estados de artículos individuales")
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
