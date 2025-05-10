// LoanService.java
package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final AlertRepository alertRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository, ArticleRepository articleRepository,AlertRepository alertRepository) {
        this.loanRepository = loanRepository;
        this.articleRepository = articleRepository;
        this.alertRepository=alertRepository;
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
                case "equipmentStatus" -> loan.setEquipmentStatus((String) value);
                case "estado", "articulo_estado" -> {} // Ya manejados
                default -> throw new IllegalArgumentException("Campo no válido: " + key);
            }
        });

        // ✅ Caso 1: Actualizar todos los artículos del préstamo
        if (updates.containsKey("equipmentStatus")) {
            String newArticleStatus = determineArticleStatus(loan.getEquipmentStatus());
            updateArticlesStatus(loan.getArticleIds(), newArticleStatus);
        }

        if (updates.containsKey("articulo_estado")) {
            Map<String, String> estadosArticulos = (Map<String, String>) updates.get("articulo_estado");
            estadosArticulos.forEach((articleIdStr, newStatus) -> {
                try {
                    Integer articleId = Integer.parseInt(articleIdStr);
                    if (!loan.getArticleIds().contains(articleId)) {
                        throw new IllegalArgumentException("El artículo " + articleId + " no pertenece al préstamo " + id);
                    }
                    updateSingleArticleStatus(articleId, newStatus);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("ID de artículo inválido: " + articleIdStr, e);
                }
            });
        }

        loanValidations(loan);
        loanRepository.save(loan);
    }

    private void updateSingleArticleStatus(Integer articleId, String newStatus) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + articleId));
        article.setArticleStatus(newStatus);
        articleRepository.save(article);
    }



    private void handleStatusChange(Loan loan, String newStatus) {
        if (DEVUELTO.equals(newStatus)) {
            devolverLoan(loan.getId());
        } else if (VENCIDO.equals(newStatus)) {
            markAsVencido(loan);
        }
    }

    // Método nuevo: Recordatorios 24h antes de la fecha límite
    @Scheduled(cron = "0 0 9 * * *") // Ejecuta diario a las 9:00 AM
    public void enviarRecordatoriosDevolucion() {
        LocalDate fechaRecordatorio = LocalDate.now().plusDays(1);

        List<Loan> prestamos = loanRepository.findByLoanStatusAndDevolutionDate(
                PRESTADO,
                fechaRecordatorio
        );

        prestamos.forEach(prestamo -> {
            Alert alerta = new Alert(
                    null,
                    prestamo.getUserId(),
                    "Recordatorio: Devolución pendiente para mañana (" + fechaRecordatorio + ")",
                    LocalDateTime.now()
            );
            alertRepository.save(alerta);
        });
    }

    // Método nuevo: Verificación diaria de préstamos vencidos
    @Scheduled(cron = "0 0 9 * * *") // Ejecuta diario a las 9:00 AM
    public void verificarPrestamosVencidos() {
        List<Loan> prestamosVencidos = loanRepository.findByLoanStatusAndDevolutionDateBefore(
                PRESTADO,
                LocalDate.now()
        );

        prestamosVencidos.forEach(prestamo -> {
            this.markAsVencido(prestamo);

            Alert alerta = new Alert(
                    null,
                    prestamo.getUserId(),
                    "Alerta: Préstamo vencido desde " + prestamo.getDevolutionDate(),
                    LocalDateTime.now()
            );
            alertRepository.save(alerta);
        });
    }

    // Modificar método existente para incluir alerta
    @Transactional
    public void markAsVencido(Loan loan) {
        loan.setLoanStatus(VENCIDO);
        updateArticlesStatus(loan.getArticleIds(), "Disponible");
        loanRepository.save(loan);

        // Nueva alerta por estado vencido
        Alert alerta = new Alert(
                null,
                loan.getUserId(),
                "Préstamo marcado como vencido: " + loan.getId(),
                LocalDateTime.now()
        );
        alertRepository.save(alerta);
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
    @Transactional
    public void updateArticlesStatus(String loanId, Map<String, String> articulosUpdate) {
        Loan loan = getLoanById(loanId);
        List<Integer> validArticleIds = loan.getArticleIds();

        articulosUpdate.forEach((articleIdStr, newStatus) -> {
            try {
                Integer articleId = Integer.parseInt(articleIdStr);

                if (!validArticleIds.contains(articleId)) {
                    throw new IllegalArgumentException("Artículo " + articleId + " no pertenece al préstamo");
                }

                if (!isValidArticleStatus(newStatus)) {
                    throw new IllegalArgumentException("Estado inválido para artículo: " + newStatus);
                }

                articleRepository.findById(articleId).ifPresent(article -> {
                    article.setArticleStatus(newStatus);
                    articleRepository.save(article);
                });

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de artículo inválido: " + articleIdStr);
            }
        });
    }

    private boolean isValidArticleStatus(String status) {
        return List.of("Disponible", "Dañado", "RequiereMantenimiento", "Prestado", "Perdido")
                .contains(status);
    }



}