package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    public static final String PRESTADO_STATUS = "Prestado";
    public static final String VENCIDO_STATUS = "Vencido";
    public static final String DEVUELTO_STATUS = "Devuelto";
    public static final String ID_NULL = "ID no deberia ser nulo";
    public static final String STATUS = "estado";
    public static final String ARTICLE_STATUS = "articulo_estado";

    


    // Enum para estados de préstamo para mayor seguridad de tipo
    public enum LoanStatus {
        PRESTADO(PRESTADO_STATUS),
        VENCIDO(VENCIDO_STATUS),
        DEVUELTO(DEVUELTO_STATUS);

        private final String value;

        LoanStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean isValid(String status) {
            return Arrays.stream(values())
                    .map(LoanStatus::getValue)
                    .anyMatch(val -> val.equals(status));
        }
    }

    // Enum para estados de artículos
    public enum ArticleStatus {
        DISPONIBLE("Disponible"),
        DANADO("Dañado"),
        REQUIERE_MANTENIMIENTO("RequiereMantenimiento"),
        PRESTADO(PRESTADO_STATUS),
        PERDIDO("Perdido");

        private final String value;

        ArticleStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean isValid(String status) {
            return Arrays.stream(values())
                    .map(ArticleStatus::getValue)
                    .anyMatch(val -> val.equals(status));
        }
    }

    private final LoanRepository loanRepository;
    private final ArticleRepository articleRepository;
    private final AlertRepository alertRepository;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public LoanService(LoanRepository loanRepository, ArticleRepository articleRepository, AlertRepository alertRepository, MongoTemplate mongoTemplate, MongoTemplate mongoTemplate1) {
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository must not be null");
        this.articleRepository = Objects.requireNonNull(articleRepository, "articleRepository must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "alertRepository must not be null");
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public Loan createLoan(Loan loan) {
        Objects.requireNonNull(loan, "loan must not be null");

        validateArticlesForLoan(loan.getArticleIds());
        configureLoanDates(loan);
        validateLoanFields(loan);
        updateArticlesStatus(loan.getArticleIds(), LoanStatus.PRESTADO.getValue());

        return loanRepository.save(loan);
    }

    private void validateArticlesForLoan(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new LoanException.LoanExceptionStateError("El préstamo debe contener al menos un artículo");
        }

        List<Article> articles = articleRepository.findAllById(articleIds);
        if (articles.size() != articleIds.size()) {
            throw new LoanException.LoanExceptionStateError("Algunos artículos no existen");
        }

        List<Integer> unavailableArticles = articles.stream()
                .filter(article -> !ArticleStatus.DISPONIBLE.getValue().equals(article.getArticleStatus()))
                .map(Article::getId)
                .toList();

        if (!unavailableArticles.isEmpty()) {
            throw new LoanException.LoanExceptionBookIsAvailable("Los siguientes artículos no están disponibles: " + unavailableArticles);
        }
    }

    private void configureLoanDates(Loan loan) {
        if (loan.getLoanDate() == null) {
            loan.setLoanDate(LocalDate.now());
        }
        loan.setCreationDate(LocalDateTime.now());
    }

    private void validateLoanFields(Loan loan) {
        validateLoanDates(loan);
        validateLoanStatus(loan);
    }

    private void validateLoanDates(Loan loan) {
        LocalDate devolutionDate = loan.getDevolutionDate();
        if (devolutionDate != null) {
            if (loan.getLoanDate().isAfter(devolutionDate)) {
                throw new LoanException.LoanExceptionTimeError("La fecha de préstamo no puede ser posterior a la de devolución");
            }
            if (devolutionDate.isBefore(LocalDate.now())) {
                throw new LoanException.LoanExceptionTimeError("La fecha de devolución no puede ser en el pasado");
            }
        }
    }

    private void validateLoanStatus(Loan loan) {
        String status = loan.getLoanStatus();
        if (status == null || !LoanStatus.isValid(status)) {
            throw new LoanException.LoanExceptionStateError("Estado de préstamo inválido: " + status);
        }
    }

    @Transactional
    public void devolverLoan(String loanId) {
        Objects.requireNonNull(loanId, "loan Id must not be null");

        Loan loan = getLoanById(loanId);
        loan.setLoanStatus(LoanStatus.DEVUELTO.getValue());
        loan.setDevolutionDate(LocalDate.now());

        String newArticleStatus = determineArticleStatus(loan.getEquipmentStatus());
        updateArticlesStatus(loan.getArticleIds(), newArticleStatus);

        loanRepository.save(loan);
    }

    String determineArticleStatus(String equipmentStatus) {
        if (equipmentStatus == null) {
            return ArticleStatus.DISPONIBLE.getValue();
        }

        return switch (equipmentStatus) {
            case "Dañado" -> ArticleStatus.DANADO.getValue();
            case "Requiere mantenimiento" -> ArticleStatus.REQUIERE_MANTENIMIENTO.getValue();
            default -> ArticleStatus.DISPONIBLE.getValue();
        };
    }

    @Transactional
    public Loan deleteLoanById(String id) {
        Objects.requireNonNull(id, ID_NULL);

        Loan loan = getLoanById(id);
        if (loan == null) {
            throw new LoanException("Error con el préstamo " + id);
        }
        validateDeletion(loan);

        if (LoanStatus.PRESTADO.getValue().equals(loan.getLoanStatus())) {
            updateArticlesStatus(loan.getArticleIds(), ArticleStatus.DISPONIBLE.getValue());
        }

        loanRepository.delete(loan);
        return loan;
    }

    private void validateDeletion(Loan loan) {
        if (LoanStatus.DEVUELTO.getValue().equals(loan.getLoanStatus())) {
            throw new LoanException.LoanExceptionStateError("No se puede eliminar un préstamo devuelto");
        }
        if (LoanStatus.VENCIDO.getValue().equals(loan.getLoanStatus())) {
            throw new LoanException.LoanExceptionStateError("No se puede eliminar un préstamo vencido");
        }
    }

    @Transactional
    public void updateLoan(String id, Map<String, Object> updates) {
        Objects.requireNonNull(id, ID_NULL);
        Objects.requireNonNull(updates, "updates must not be null");

        Loan loan = getLoanById(id);

        // Handle status change separately
        if (updates.containsKey(STATUS)) {
            handleStatusChangeForLoan(loan, updates);
        }

        // Handle field updates in a separate method
        processFieldUpdates(loan, updates);

        // Handle article updates in separate methods
        updateArticleStatusIfNeeded(loan, updates);
        updateArticleStatesIfNeeded(loan, updates);

        // Validate and save the loan
        validateLoanFields(loan);
        loanRepository.save(loan);
    }

    private void handleStatusChangeForLoan(Loan loan, Map<String, Object> updates) {
        Object estadoRaw = updates.get(STATUS);
        if (estadoRaw instanceof String estado && !estado.trim().isEmpty()) {
            handleStatusChange(loan, estado.trim());
        } else {
            throw new IllegalArgumentException("El estado proporcionado no es válido.");
        }
    }

    private void processFieldUpdates(Loan loan, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            if (value == null) return; // Ignore null values

            switch (key) {
                case "observaciones" -> handleObservacionesUpdate(loan, value);
                case "fecha_devolucion" -> loan.setDevolutionDate(parseDate(value));
                case "equipmentStatus" -> handleEquipmentStatusUpdate(loan, value);
                case STATUS, ARTICLE_STATUS -> logger.debug("Campo '{}' ya manejado previamente, se omite.", key);
                default -> throw new IllegalArgumentException("Campo no válido: " + key + ". Campos válidos: observaciones, fecha_devolucion, equipmentStatus, estado, articulo_estado");
            }
        });
    }

    private void handleObservacionesUpdate(Loan loan, Object value) {
        if (value instanceof String v) {
            loan.setLoanDescriptionType(v);
        } else {
            throw new IllegalArgumentException("Valor inválido para observaciones");
        }
    }

    private void handleEquipmentStatusUpdate(Loan loan, Object value) {
        if (value instanceof String v) {
            loan.setEquipmentStatus(v);
        } else {
            throw new IllegalArgumentException("Valor inválido para equipmentStatus");
        }
    }

    private void updateArticleStatusIfNeeded(Loan loan, Map<String, Object> updates) {
        if (updates.containsKey("equipmentStatus")) {
            String newArticleStatus = determineArticleStatus(loan.getEquipmentStatus());
            updateArticlesStatus(loan.getArticleIds(), newArticleStatus);
        }
    }

    private void updateArticleStatesIfNeeded(Loan loan, Map<String, Object> updates) {
        if (updates.containsKey(ARTICLE_STATUS)) {
            updateArticleStatesFromMap(loan, updates.get(ARTICLE_STATUS));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateArticleStatesFromMap(Loan loan, Object articleStatesObj) {
        if (!(articleStatesObj instanceof Map)) {
            throw new IllegalArgumentException("articulo_estado debe ser un mapa");
        }

        Map<String, String> estadosArticulos = (Map<String, String>) articleStatesObj;
        estadosArticulos.forEach((articleIdStr, newStatus) -> {
            try {
                Integer articleId = Integer.parseInt(articleIdStr);
                if (!loan.getArticleIds().contains(articleId)) {
                    throw new IllegalArgumentException("El artículo " + articleId + " no pertenece al préstamo " + loan.getId());
                }
                updateSingleArticleStatus(articleId, newStatus);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de artículo inválido: " + articleIdStr);
            }
        });
    }

    private void updateSingleArticleStatus(Integer articleId, String newStatus) {
        if (!ArticleStatus.isValid(newStatus)) {
            logger.warn("Intento de actualizar con estado inválido: {}", newStatus);
            throw new IllegalArgumentException("Estado de artículo inválido: " + newStatus);
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalStateException("Artículo no encontrado: " + articleId));

        article.setArticleStatus(newStatus);
        articleRepository.save(article);
    }

    private void handleStatusChange(Loan loan, String newStatus) {
        if (!LoanStatus.isValid(newStatus)) {
            logger.warn("Estado de préstamo inválido recibido: {}", newStatus);
            throw new IllegalArgumentException("Estado de préstamo inválido: " + newStatus);
        }

        switch (newStatus) {
            case DEVUELTO_STATUS -> devolverLoan(loan.getId());
            case VENCIDO_STATUS -> markAsVencido(loan);
            default -> logger.info("Estado de préstamo cambiado a '{}'. No requiere acción adicional.", newStatus);
        }
    }


    // Método para recordatorios 24h antes de fecha límite
    @Scheduled(cron = "0 0 9 * * *") // Ejecuta diario a las 9:00 AM
    @Transactional
    public void enviarRecordatoriosDevolucion() {
        LocalDate fechaRecordatorio = LocalDate.now().plusDays(1);

        List<Loan> prestamos = loanRepository.findByLoanStatusAndDevolutionDate(
                LoanStatus.PRESTADO.getValue(),
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

    // Método para verificación diaria de préstamos vencidos
    @Scheduled(cron = "0 0 9 * * *") // Ejecuta diario a las 9:00 AM
    @Transactional
    public void verificarPrestamosVencidos() {
        List<Loan> prestamosVencidos = loanRepository.findByLoanStatusAndDevolutionDateBefore(
                LoanStatus.PRESTADO.getValue(),
                LocalDate.now()
        );

        prestamosVencidos.forEach(this::markAsVencido);
    }

   @Transactional
    public void markAsVencido(Loan loan) {
        Objects.requireNonNull(loan, "loan must not be null");

        loan.setLoanStatus(LoanStatus.VENCIDO.getValue());
        updateArticlesStatus(loan.getArticleIds(), ArticleStatus.DISPONIBLE.getValue());
        loanRepository.save(loan);

        alertRepository.save(new Alert(
                null,
                loan.getUserId(),
                String.format("Préstamo marcado como vencido: %s", loan.getId()),
                LocalDateTime.now()
        ));
    }

    LocalDate parseDate(Object dateValue) {
        if (dateValue == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }

        if (dateValue instanceof String stringDate) {
            try {
                return LocalDate.parse(stringDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha inválido: " + stringDate, e);
            }
        }

        if (dateValue instanceof LocalDate localDate) {
            return localDate;
        }

        throw new IllegalArgumentException("Tipo de fecha inválido: " + dateValue.getClass().getSimpleName());
    }


    private void updateArticlesStatus(List<Integer> articleIds, String newStatus) {
        if (articleIds == null || articleIds.isEmpty()) {
            logger.warn("Intento de actualizar estado con lista de artículos vacía o nula.");
            return;
        }

        List<Article> articles = articleRepository.findAllById(articleIds);
        articles.forEach(article -> article.setArticleStatus(newStatus));
        articleRepository.saveAll(articles);
    }


    // Métodos de consulta
    public List<Loan> getLoans(String status) {
        if (status == null || status.isBlank()) {
            return loanRepository.findAll();
        }

        return switch (status) {
            case PRESTADO_STATUS -> loanRepository.findByLoanStatus(LoanStatus.PRESTADO.getValue());
            case VENCIDO_STATUS -> loanRepository.findByLoanStatus(LoanStatus.VENCIDO.getValue());
            case DEVUELTO_STATUS -> loanRepository.findByLoanStatus(LoanStatus.DEVUELTO.getValue());
            default -> {
                logger.warn("Estado desconocido recibido en consulta de préstamos. Se retornan todos.");
                yield loanRepository.findAll();
            }
        };
    }

    public Loan getLoanById(String id) {
        Objects.requireNonNull(id, ID_NULL);

        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanException.LoanExceptionPrestamoIdNotFound("Préstamo no encontrado con ID: " + id));
    }

    public List<Loan> getLoansByUser(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        List<Loan> loans = loanRepository.findByUserId(userId);
        if (loans.isEmpty()) {
            throw new LoanException.LoanExceptionEstudianteHasNotPrestamo("El usuario no tiene préstamos registrados: " + userId);
        }
        return loans;
    }

    public Object getAvailableArticlesInInterval(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Loan> overlappingLoans = loanRepository.findOverlappingLoans(
                LoanStatus.PRESTADO.getValue(), startDate, endDate);

        Set<Integer> unavailableArticleIds = overlappingLoans.stream()
                .flatMap(loan -> loan.getArticleIds().stream())
                .collect(Collectors.toSet());

        return unavailableArticleIds.isEmpty()
                ? articleRepository.findByArticleStatus(ArticleStatus.DISPONIBLE.getValue())
                : articleRepository.findByArticleStatusAndIdNotIn(ArticleStatus.DISPONIBLE.getValue(), unavailableArticleIds);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    // En LoanService
    public List<Loan> getLoansByDateRangeAndStatus(LocalDate startDate, LocalDate endDate, String status) {
        // Agrega logs para depuración
        logger.info("Buscando préstamos entre {} y {} con estado: {}", startDate, endDate, status);

        try {
            // Construir la consulta para MongoDB
            Query query = new Query();

            // Criterio para el rango de fechas
            Criteria dateCriteria = Criteria.where("loanDate").gte(startDate).lte(endDate);
            query.addCriteria(dateCriteria);

            // Agregar criterio de estado si se proporciona
            if (status != null && !status.isEmpty()) {
                query.addCriteria(Criteria.where("loanStatus").is(status));
            }

            // Ejecutar consulta
            List<Loan> loans = mongoTemplate.find(query, Loan.class);

            // Log para depuración
            logger.info("Se encontraron {} préstamos en el rango de fechas", loans.size());

            return loans;
        } catch (Exception e) {
            logger.error("Error al buscar préstamos por rango de fechas", e);
            throw new LoanException("Error al buscar préstamos por rango de fechas: " + e.getMessage());
        }
    }

    public List<Loan> getLoansByUserReport(String userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return loanRepository.findByUserId(userId);
    }

    @Transactional
    public void updateArticlesStatus(String loanId, Map<String, String> articulosUpdate) {
        Objects.requireNonNull(loanId, "loanId must not be null");
        Objects.requireNonNull(articulosUpdate, "articulosUpdate must not be null");

        Loan loan = getLoanById(loanId);
        List<Integer> validArticleIds = loan.getArticleIds();

        articulosUpdate.forEach((articleIdStr, newStatus) -> {
            try {
                Integer articleId = Integer.parseInt(articleIdStr);

                if (!validArticleIds.contains(articleId)) {
                    throw new IllegalArgumentException("Artículo " + articleId + " no pertenece al préstamo");
                }

                if (!ArticleStatus.isValid(newStatus)) {
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
}