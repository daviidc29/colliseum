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

    @GetMapping
    @Operation(summary = "Obtener todos los préstamos", description = "Recupera una lista de todos los préstamos, opcionalmente filtrados por estado.")
    public ResponseEntity<?> getPrestamos(
        @Parameter(description = "Estado opcional del préstamo (ej. activo, devuelto)", required = false)
        @RequestParam(value = "status", required = false) String status) { 
        try {
            return ResponseEntity.ok(Collections.singletonMap("loan", loanService.getLoans(status)));
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener préstamo por ID", description = "Devuelve los detalles de un préstamo específico según su ID.")
    public ResponseEntity<?> getLoanById(
        @Parameter(description = "ID del préstamo", required = true)
        @PathVariable String id) {
        try {
            return ResponseEntity.ok(Collections.singletonMap("loan", loanService.getLoanById(id)));
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

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

    @PatchMapping("/{id}/devolver")
    @Operation(summary = "Devolver préstamo", description = "Marca un préstamo como devuelto.")
    public ResponseEntity<?> devolverLoan(
        @Parameter(description = "ID del préstamo a devolver", required = true)
        @PathVariable String id) {
        try {
            loanService.devolverLoan(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Préstamo devuelto correctamente"));
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (ArticleException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error inesperado"));
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

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar préstamo", description = "Actualiza campos específicos de un préstamo mediante un mapa de claves y valores.")
    public ResponseEntity<?> updateLoan(
        @Parameter(description = "ID del préstamo a actualizar", required = true)
        @Valid @PathVariable String id,
        @Parameter(description = "Campos a actualizar en el préstamo", required = true)
        @Valid @RequestBody Map<String, Object> updates) {
        try {
            loanService.updateLoan(id, updates);
            return ResponseEntity.ok(Collections.singletonMap("message", "Préstamo actualizado correctamente"));
        } catch (IllegalArgumentException | LoanException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error inesperado"));
        }
    }


    @GetMapping("/date-range")
    @Operation(summary = "Buscar préstamos por rango de fechas", description = "Recupera los préstamos realizados entre dos fechas opcionalmente filtrados por estado.")
    public ResponseEntity<?> getLoansByDateRange(
        @Parameter(description = "Fecha de inicio", required = true)
        @RequestParam LocalDate startDate,
        @Parameter(description = "Fecha de fin", required = true)
        @RequestParam LocalDate endDate,
        @Parameter(description = "Estado opcional del préstamo", required = false)
        @RequestParam(required = false) String status) {
        try {
            List<Loan> loans = loanService.getLoansByDateRangeAndStatus(startDate, endDate, status);
            return ResponseEntity.ok(Collections.singletonMap("loans", loans));
        } catch (LoanException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener préstamos por usuario", description = "Recupera todos los préstamos asociados a un usuario específico.")
    public ResponseEntity<?> getLoansByUserReport(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable String userId) {
        List<Loan> loans = loanService.getLoansByUserReport(userId);
        return ResponseEntity.ok(Collections.singletonMap("loans", loans));
    }
}
