package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(loanRepository, articleRepository, alertRepository, mongoTemplate, mongoTemplate);
    }

    private Loan createValidLoan() {
        return Loan.builder()
                .id("loan1")
                .articleIds(List.of(1))
                .nameUser("user")
                .userId("uid")
                .userRole("Estudiante")
                .loanDescriptionType("desc")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.now())
                .devolutionDate(LocalDate.now().plusDays(2))
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();
    }

    private Article createAvailableArticle() {
        return Article.builder()
                .id(1)
                .name("Articulo1")
                .articleStatus("Disponible")
                .description("desc")
                .imageUrl("img.jpg")
                .build();
    }

    @Test
    void testCreateLoanSuccess() {
        Loan loan = createValidLoan();
        Article article = createAvailableArticle();

        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanRepository.save(any())).thenReturn(loan);

        Loan savedLoan = loanService.createLoan(loan);

        assertNotNull(savedLoan);
        verify(articleRepository).saveAll(anyList());
        verify(loanRepository).save(loan);
    }

    @Test
    void testCreateLoanWithUnavailableArticleThrows() {
        Loan loan = createValidLoan();
        Article unavailable = createAvailableArticle();
        unavailable.setArticleStatus("Prestado");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(unavailable));
        assertThrows(LoanException.LoanExceptionBookIsAvailable.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoanWithMissingArticlesThrows() {
        Loan loan = createValidLoan();
        when(articleRepository.findAllById(List.of(1))).thenReturn(Collections.emptyList());
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoanWithNullArticlesThrows() {
        Loan loan = createValidLoan();
        loan.setArticleIds(null);
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoanWithLoanDateAfterDevolutionThrows() {
        Loan loan = createValidLoan();
        loan.setLoanDate(LocalDate.now().plusDays(10));
        loan.setDevolutionDate(LocalDate.now());
        Article a = createAvailableArticle();
        when(articleRepository.findAllById(any())).thenReturn(List.of(a));
        assertThrows(LoanException.LoanExceptionTimeError.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoanWithDevolutionDateInPastThrows() {
        Loan loan = createValidLoan();
        loan.setDevolutionDate(LocalDate.now().minusDays(1));
        Article a = createAvailableArticle();
        when(articleRepository.findAllById(any())).thenReturn(List.of(a));
        assertThrows(LoanException.LoanExceptionTimeError.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoanWithInvalidStatusThrows() {
        Loan loan = createValidLoan();
        loan.setLoanStatus("INVALIDO");
        Article a = createAvailableArticle();
        when(articleRepository.findAllById(any())).thenReturn(List.of(a));
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testDevolverLoan() {
        Loan loan = createValidLoan();
        loan.setLoanStatus("Prestado");
        loan.setEquipmentStatus(null);

        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        when(loanRepository.save(any())).thenReturn(loan);

        loanService.devolverLoan("loan1");

        assertEquals("Devuelto", loan.getLoanStatus());
        verify(loanRepository).save(loan);
        verify(articleRepository).saveAll(anyList());
    }

    @Test
    void testDevolverLoanWithEquipmentStatus() {
        Loan loan = createValidLoan();
        loan.setEquipmentStatus("Dañado");
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        loanService.devolverLoan("loan1");
        verify(articleRepository).saveAll(anyList());
    }

    @Test
    void testDeleteLoanByIdPrestado() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Loan deleted = loanService.deleteLoanById("loan1");
        assertEquals(loan, deleted);
        verify(articleRepository).saveAll(anyList());
        verify(loanRepository).delete(loan);
    }

    @Test
    void testDeleteLoanByIdDevueltoThrows() {
        Loan loan = createValidLoan();
        loan.setLoanStatus("Devuelto");
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanService.deleteLoanById("loan1"));
    }

    @Test
    void testDeleteLoanByIdNotFoundThrows() {
        when(loanRepository.findById("loan1")).thenReturn(Optional.empty());
        assertThrows(LoanException.class, () -> loanService.deleteLoanById("loan1"));
    }

    @Test
    void testDeleteLoanByIdVencidoThrows() {
        Loan loan = createValidLoan();
        loan.setLoanStatus("Vencido");
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanService.deleteLoanById("loan1"));
    }

    @Test
    void testUpdateLoanObservaciones() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("observaciones", "Nueva observacion");
        loanService.updateLoan("loan1", updates);
        assertEquals("Nueva observacion", loan.getLoanDescriptionType());
        verify(loanRepository).save(loan);
    }

    @Test
    void testUpdateLoanFechaDevolucion() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("fecha_devolucion", LocalDate.now().plusDays(5));
        loanService.updateLoan("loan1", updates);
        assertEquals(LocalDate.now().plusDays(5), loan.getDevolutionDate());
        verify(loanRepository).save(loan);
    }

    @Test
    void testUpdateLoanEquipmentStatus() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("equipmentStatus", "Dañado");
        loanService.updateLoan("loan1", updates);
        assertEquals("Dañado", loan.getEquipmentStatus());
        verify(articleRepository).saveAll(anyList());
    }

    @Test
    void testUpdateLoanInvalidFieldThrows() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("invalido", "valor");
        assertThrows(IllegalArgumentException.class, () -> loanService.updateLoan("loan1", updates));
    }

    @Test
    void testUpdateLoanInvalidArticleStatusThrows() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> articleStates = new HashMap<>();
        articleStates.put("1", "Invalido");
        updates.put("articulo_estado", articleStates);
        assertThrows(IllegalArgumentException.class, () -> loanService.updateLoan("loan1", updates));
    }

    @Test
    void testUpdateLoanStatusDevuelto() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Devuelto");
        loanService.updateLoan("loan1", updates);
        verify(loanRepository, atLeastOnce()).save(any());
    }

    @Test
    void testUpdateLoanStatusVencido() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Vencido");
        loanService.updateLoan("loan1", updates);
        verify(loanRepository, atLeastOnce()).save(any());
    }

    @Test
    void testUpdateLoanStatusInvalidThrows() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Invalido");
        assertThrows(IllegalArgumentException.class, () -> loanService.updateLoan("loan1", updates));
    }

    @Test
    void testDetermineArticleStatus() {
        assertEquals("Disponible", loanService.determineArticleStatus(null));
        assertEquals("Dañado", loanService.determineArticleStatus("Dañado"));
        assertEquals("RequiereMantenimiento", loanService.determineArticleStatus("Requiere mantenimiento"));
        assertEquals("Disponible", loanService.determineArticleStatus("Otro"));
    }

    @Test
    void testEnviarRecordatoriosDevolucion() {
        Loan loan = createValidLoan();
        when(loanRepository.findByLoanStatusAndDevolutionDate(anyString(), any(LocalDate.class))).thenReturn(List.of(loan));
        when(alertRepository.save(any(Alert.class))).thenReturn(null);
        loanService.enviarRecordatoriosDevolucion();
        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
    }

    @Test
    void testVerificarPrestamosVencidos() {
        Loan loan = createValidLoan();
        when(loanRepository.findByLoanStatusAndDevolutionDateBefore(anyString(), any(LocalDate.class))).thenReturn(List.of(loan));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        loanService.verificarPrestamosVencidos();
        verify(loanRepository, atLeastOnce()).save(any());
        verify(alertRepository, atLeastOnce()).save(any());
    }

    @Test
    void testMarkAsVencido() {
        Loan loan = createValidLoan();
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        loanService.markAsVencido(loan);
        assertEquals("Vencido", loan.getLoanStatus());
        verify(loanRepository).save(loan);
        verify(alertRepository).save(any());
    }

    @Test
    void testParseDateString() {
        LocalDate date = loanService.parseDate(LocalDate.now().toString());
        assertEquals(LocalDate.now(), date);
    }

    @Test
    void testParseDateLocalDate() {
        LocalDate now = LocalDate.now();
        assertEquals(now, loanService.parseDate(now));
    }

    @Test
    void testParseDateInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> loanService.parseDate(1));
        assertThrows(IllegalArgumentException.class, () -> loanService.parseDate(null));
        assertThrows(IllegalArgumentException.class, () -> loanService.parseDate("no-date"));
    }

    @Test
    void testUpdateArticlesStatus() {
        Loan loan = createValidLoan();
        Article article = createAvailableArticle();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findById(1)).thenReturn(Optional.of(article));
        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("1", "Prestado");
        loanService.updateArticlesStatus("loan1", articulosUpdate);
        verify(articleRepository).save(article);
    }

    @Test
    void testUpdateArticlesStatusWithInvalidIdThrows() {
        Loan loan = createValidLoan();
        loan.setArticleIds(List.of(1));
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("2", "Prestado"); // not in loan
        assertThrows(IllegalArgumentException.class, () -> loanService.updateArticlesStatus("loan1", articulosUpdate));
    }

    @Test
    void testUpdateArticlesStatusWithInvalidStatusThrows() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("1", "NO_VALIDO");
        assertThrows(IllegalArgumentException.class, () -> loanService.updateArticlesStatus("loan1", articulosUpdate));
    }

    @Test
    void testGetLoansByStatus() {
        when(loanRepository.findByLoanStatus("Prestado")).thenReturn(List.of(createValidLoan()));
        List<Loan> loans = loanService.getLoans("Prestado");
        assertFalse(loans.isEmpty());
    }

    @Test
    void testGetLoansByUnknownStatusReturnsAll() {
        when(loanRepository.findAll()).thenReturn(List.of(createValidLoan()));
        List<Loan> loans = loanService.getLoans("Desconocido");
        assertFalse(loans.isEmpty());
    }

    @Test
    void testGetLoanByIdSuccess() {
        Loan loan = createValidLoan();
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        assertEquals(loan, loanService.getLoanById("loan1"));
    }

    @Test
    void testGetLoanByIdThrows() {
        when(loanRepository.findById("loan1")).thenReturn(Optional.empty());
        assertThrows(LoanException.LoanExceptionPrestamoIdNotFound.class, () -> loanService.getLoanById("loan1"));
    }

    @Test
    void testGetLoansByUserSuccess() {
        Loan loan = createValidLoan();
        when(loanRepository.findByUserId("uid")).thenReturn(List.of(loan));
        List<Loan> loans = loanService.getLoansByUser("uid");
        assertEquals(1, loans.size());
    }

    @Test
    void testGetLoansByUserThrows() {
        when(loanRepository.findByUserId("uid")).thenReturn(Collections.emptyList());
        assertThrows(LoanException.LoanExceptionEstudianteHasNotPrestamo.class, () -> loanService.getLoansByUser("uid"));
    }

    @Test
    void testGetAvailableArticlesInInterval() {
    }

    @Test
    void testGetAvailableArticlesInIntervalWithUnavailableArticles() {
        Loan loan = createValidLoan();
        loan.setArticleIds(List.of(1,2));
        when(loanRepository.findOverlappingLoans(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(loan));
        when(articleRepository.findByArticleStatusAndIdNotIn(anyString(), anySet()))
                .thenReturn(List.of(createAvailableArticle()));
        Object result = loanService.getAvailableArticlesInInterval(LocalDate.now(), LocalDate.now().plusDays(1));
        assertNotNull(result);
    }

    @Test
    void testGetAvailableArticlesInIntervalInvalidDatesThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                loanService.getAvailableArticlesInInterval(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () ->
                loanService.getAvailableArticlesInInterval(LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class, () ->
                loanService.getAvailableArticlesInInterval(LocalDate.now().plusDays(1), LocalDate.now()));
    }

    @Test
    void testGetLoansByDateRangeAndStatus() {
        when(mongoTemplate.find(any(Query.class), eq(Loan.class)))
                .thenReturn(List.of(createValidLoan()));
        List<Loan> loans = loanService.getLoansByDateRangeAndStatus(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "Prestado");
        assertFalse(loans.isEmpty());
    }

    @Test
    void testGetLoansByDateRangeAndStatusThrows() {
        when(mongoTemplate.find(any(Query.class), eq(Loan.class)))
                .thenThrow(new RuntimeException("error"));
        assertThrows(LoanException.class,
                () -> loanService.getLoansByDateRangeAndStatus(LocalDate.now(), LocalDate.now(), "Prestado"));
    }

    @Test
    void testGetLoansByUserReport() {
        Loan loan = createValidLoan();
        when(loanRepository.findByUserId("uid")).thenReturn(List.of(loan));
        List<Loan> loans = loanService.getLoansByUserReport("uid");
        assertEquals(1, loans.size());
    }
}