package edu.eci.cvds.proyect.coliseum.persistency.Controller;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
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

public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public ResponseEntity<?> getPrestamos(@RequestParam(value = "status", required = false) String status) {
        try {
            return ResponseEntity.ok(Collections.singletonMap("loan", loanService.getLoans(status)));
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(Collections.singletonMap("loan", loanService.getLoanById(id)));
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createLoan(@Valid @RequestBody Loan loan) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("loan", loanService.createLoan(loan)));
        } catch (LoanException | ArticleException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/devolver")
    public ResponseEntity<?> devolverLoan(@PathVariable String id) {
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
    public ResponseEntity<?> deleteLoan(@PathVariable String id) {
        try {
            loanService.deleteLoanById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (LoanException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateLoan(@Valid@PathVariable String id, @Valid@RequestBody Map<String, Object> updates) {
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
    public ResponseEntity<?> getLoansByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String status) {
        try {
            List<Loan> loans = loanService.getLoansByDateRangeAndStatus(startDate, endDate, status);
            return ResponseEntity.ok(Collections.singletonMap("loans", loans));
        } catch (LoanException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getLoansByUserReport(@PathVariable String userId) {
        List<Loan> loans = loanService.getLoansByUserReport(userId);
        return ResponseEntity.ok(Collections.singletonMap("loans", loans));
    }
}
