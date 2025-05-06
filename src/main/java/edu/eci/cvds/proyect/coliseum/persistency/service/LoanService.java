// LoanService.java
package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanService {
    public static final String PRESTADO = "Prestado";
    public static final String VENCIDO = "Vencido";
    public static final String DEVUELTO = "Devuelto";

    private final LoanRepository loanRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository, ArticleRepository articleRepository) {
        this.loanRepository = loanRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public Loan createLoan(Loan loan) {
        validateArticlesForLoan(loan.getArticleIds());
        configureLoanDates(loan);
        loanValidations(loan);
        updateArticlesStatus(loan.getArticleIds(), PRESTADO);
        return loanRepository.save(loan);
    }

    private void validateArticlesForLoan(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new LoanException("El préstamo debe contener al menos un artículo");
        }

        List<Article> articles = articleRepository.findAllById(articleIds);
        if (articles.size() != articleIds.size()) {
            throw new LoanException("Algunos artículos no existen");
        }

        List<Integer> noDisponibles = articles.stream()
                .filter(article -> !"Disponible".equals(article.getArticleStatus()))
                .map(Article::getId)
                .toList();

        if (!noDisponibles.isEmpty()) {
            throw new LoanException("Los siguientes artículos no están disponibles: " + noDisponibles);
        }
    }


    private void configureLoanDates(Loan loan) {
        if (loan.getLoanDate() == null) {
            loan.setLoanDate(LocalDate.now());
        }
        loan.setCreationDate(LocalDateTime.now());
    }

    private void loanValidations(Loan loan) {
        if (loan.getDevolutionDate() != null) {
            if (loan.getLoanDate().isAfter(loan.getDevolutionDate())) {
                throw new LoanException("La fecha de préstamo no puede ser posterior a la de devolución");
            }
            if (loan.getDevolutionDate().isBefore(LocalDate.now())) {
                throw new LoanException("La fecha de devolución no puede ser en el pasado");
            }
        }

        if (!loan.getLoanStatus().matches(PRESTADO + "|" + VENCIDO + "|" + DEVUELTO)) {
            throw new LoanException("Estado de préstamo inválido");
        }
    }

    @Transactional
    public void devolverLoan(String loanId) {
        Loan loan = getLoanById(loanId);
        loan.setLoanStatus(DEVUELTO);
        loan.setDevolutionDate(LocalDate.now());

        String newArticleStatus = determineArticleStatus(loan.getEquipmentStatus());
        updateArticlesStatus(loan.getArticleIds(), newArticleStatus);

        loanRepository.save(loan);
    }

    private String determineArticleStatus(String equipmentStatus) {
        return switch (equipmentStatus) {
            case "Dañado" -> "Dañado";
            case "Requiere mantenimiento" -> "RequiereMantenimiento";
            default -> "Disponible";
        };
    }

    @Transactional
    public Loan deleteLoanById(String id) {
        Loan loan = getLoanById(id);
        validateDeletion(loan);

        if (PRESTADO.equals(loan.getLoanStatus())) {
            updateArticlesStatus(loan.getArticleIds(), "Disponible");
        }

        loanRepository.delete(loan);
        return loan;
    }

    private void validateDeletion(Loan loan) {
        if (DEVUELTO.equals(loan.getLoanStatus())) {
            throw new LoanException("No se puede eliminar un préstamo devuelto");
        }
        if (VENCIDO.equals(loan.getLoanStatus())) {
            throw new LoanException("No se puede eliminar un préstamo vencido");
        }
    }

    @Transactional
    public void updateLoan(String id, Map<String, Object> updates) {
        Loan loan = getLoanById(id);

        if (updates.containsKey("estado")) {
            handleStatusChange(loan, (String) updates.get("estado"));
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "observaciones" -> loan.setLoanDescriptionType((String) value);
                case "fecha_devolucion" -> loan.setDevolutionDate(parseDate(value));
                case "estado" -> {} // Ya manejado
                default -> throw new IllegalArgumentException("Campo no válido: " + key);
            }
        });

        loanValidations(loan);
        loanRepository.save(loan);
    }

    private void handleStatusChange(Loan loan, String newStatus) {
        if (DEVUELTO.equals(newStatus)) {
            devolverLoan(loan.getId());
        } else if (VENCIDO.equals(newStatus)) {
            markAsVencido(loan);
        }
    }

    @Transactional
    public void markAsVencido(Loan loan) {
        loan.setLoanStatus(VENCIDO);
        updateArticlesStatus(loan.getArticleIds(), "Disponible");
        loanRepository.save(loan);
    }

    private LocalDate parseDate(Object dateValue) {
        if (dateValue instanceof String) {
            return LocalDate.parse((String) dateValue);
        } else if (dateValue instanceof LocalDate) {
            return (LocalDate) dateValue;
        }
        throw new IllegalArgumentException("Formato de fecha inválido");
    }

    private void updateArticlesStatus(List<Integer> articleIds, String newStatus) {
        List<Article> articles = articleRepository.findAllById(articleIds);
        articles.forEach(article -> article.setArticleStatus(newStatus));
        articleRepository.saveAll(articles);
    }

    // Métodos de consulta
    public List<Loan> getLoans(String status) {
        return switch (status != null ? status : "") {
            case "Prestado" -> loanRepository.findByLoanStatus(PRESTADO);
            case "Vencido" -> loanRepository.findByLoanStatus(VENCIDO);
            case "Devuelto" -> loanRepository.findByLoanStatus(DEVUELTO);
            default -> loanRepository.findAll();
        };
    }

    public Loan getLoanById(String id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanException("Préstamo no encontrado"));
    }

    public List<Loan> getLoansByUser(String userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        if (loans.isEmpty()) {
            throw new LoanException("El usuario no tiene préstamos registrados");
        }
        return loans;
    }

    public Object getAvailableArticlesInInterval(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        List<Loan> overlappingLoans = loanRepository.findOverlappingLoans(PRESTADO, startDate, endDate);
        Set<Integer> unavailableArticleIds = overlappingLoans.stream()
                .flatMap(loan -> loan.getArticleIds().stream())
                .collect(Collectors.toSet());

        return unavailableArticleIds.isEmpty()
                ? articleRepository.findByArticleStatus("Disponible")
                : articleRepository.findByArticleStatusAndIdNotIn("Disponible", unavailableArticleIds);
    }

    public List<Loan> getLoansByDateRangeAndStatus(LocalDate startDate, LocalDate endDate, String status) {
        if (startDate == null || endDate == null) {
            throw new LoanException("Las fechas de inicio y fin son requeridas.");
        }
        if (startDate.isAfter(endDate)) {
            throw new LoanException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return (status == null || status.isEmpty())
                ? loanRepository.findByLoanDateBetween(startDate, endDate)
                : loanRepository.findByLoanDateBetweenAndLoanStatus(startDate, endDate, status);
    }

    public List<Loan> getLoansByUserReport(String userId) {
        return loanRepository.findByUserId(userId);
    }
}