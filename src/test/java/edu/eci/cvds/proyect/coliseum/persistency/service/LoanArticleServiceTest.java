package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanArcticleRepository;
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

class LoanArticleServiceTest {

    @Mock private LoanArcticleRepository loanArcticleRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks
    private LoanArticleService loanArticleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanArticleService = new LoanArticleService(loanArcticleRepository, articleRepository, alertRepository, mongoTemplate, mongoTemplate);
    }

    private LoanArticle createValidLoan() {
        return LoanArticle.builder()
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
        LoanArticle loanArticle = createValidLoan();
        Article article = createAvailableArticle();

        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanArcticleRepository.save(any())).thenReturn(loanArticle);

        LoanArticle savedLoanArticle = loanArticleService.createLoan(loanArticle);

        assertNotNull(savedLoanArticle);
        verify(articleRepository).saveAll(anyList());
        verify(loanArcticleRepository).save(loanArticle);
    }

    @Test
    void testCreateLoanWithUnavailableArticleThrows() {
        LoanArticle loanArticle = createValidLoan();
        Article unavailable = createAvailableArticle();
        unavailable.setArticleStatus("Prestado");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(unavailable));
        assertThrows(LoanException.LoanExceptionBookIsAvailable.class, () -> loanArticleService.createLoan(loanArticle));
    }

    @Test
    void testCreateLoanWithMissingArticlesThrows() {
        LoanArticle loanArticle = createValidLoan();
        when(articleRepository.findAllById(List.of(1))).thenReturn(Collections.emptyList());
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanArticleService.createLoan(loanArticle));
    }

    @Test
    void testCreateLoanWithNullArticlesThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setArticleIds(null);
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanArticleService.createLoan(loanArticle));
    }

    @Test
    void testCreateLoanWithLoanDateAfterDevolutionThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setLoanDate(LocalDate.now().plusDays(10));
        loanArticle.setDevolutionDate(LocalDate.now());
        Article a = createAvailableArticle();
        when(articleRepository.findAllById(any())).thenReturn(List.of(a));
        assertThrows(LoanException.LoanExceptionTimeError.class, () -> loanArticleService.createLoan(loanArticle));
    }

    @Test
    void testCreateLoanWithDevolutionDateInPastThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setDevolutionDate(LocalDate.now().minusDays(1));
        Article a = createAvailableArticle();
        when(articleRepository.findAllById(any())).thenReturn(List.of(a));
        assertThrows(LoanException.LoanExceptionTimeError.class, () -> loanArticleService.createLoan(loanArticle));
    }

    @Test
    void testCreateLoanWithInvalidStatusThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setLoanStatus("INVALIDO");
        Article a = createAvailableArticle();
        when(articleRepository.findAllById(any())).thenReturn(List.of(a));
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanArticleService.createLoan(loanArticle));
    }

    @Test
    void testDevolverLoan() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setLoanStatus("Prestado");
        loanArticle.setEquipmentStatus(null);

        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        when(loanArcticleRepository.save(any())).thenReturn(loanArticle);

        loanArticleService.devolverLoan("loan1");

        assertEquals("Devuelto", loanArticle.getLoanStatus());
        verify(loanArcticleRepository).save(loanArticle);
        verify(articleRepository).saveAll(anyList());
    }

    @Test
    void testDevolverLoanWithEquipmentStatus() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setEquipmentStatus("Dañado");
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        loanArticleService.devolverLoan("loan1");
        verify(articleRepository).saveAll(anyList());
    }

    @Test
    void testDeleteLoanByIdPrestado() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        LoanArticle deleted = loanArticleService.deleteLoanById("loan1");
        assertEquals(loanArticle, deleted);
        verify(articleRepository).saveAll(anyList());
        verify(loanArcticleRepository).delete(loanArticle);
    }

    @Test
    void testDeleteLoanByIdDevueltoThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setLoanStatus("Devuelto");
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanArticleService.deleteLoanById("loan1"));
    }

    @Test
    void testDeleteLoanByIdNotFoundThrows() {
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.empty());
        assertThrows(LoanException.class, () -> loanArticleService.deleteLoanById("loan1"));
    }

    @Test
    void testDeleteLoanByIdVencidoThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setLoanStatus("Vencido");
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        assertThrows(LoanException.LoanExceptionStateError.class, () -> loanArticleService.deleteLoanById("loan1"));
    }

    @Test
    void testUpdateLoanObservaciones() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, Object> updates = new HashMap<>();
        updates.put("observaciones", "Nueva observacion");
        loanArticleService.updateLoan("loan1", updates);
        assertEquals("Nueva observacion", loanArticle.getLoanDescriptionType());
        verify(loanArcticleRepository).save(loanArticle);
    }

    @Test
    void testUpdateLoanFechaDevolucion() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, Object> updates = new HashMap<>();
        updates.put("fecha_devolucion", LocalDate.now().plusDays(5));
        loanArticleService.updateLoan("loan1", updates);
        assertEquals(LocalDate.now().plusDays(5), loanArticle.getDevolutionDate());
        verify(loanArcticleRepository).save(loanArticle);
    }

    @Test
    void testUpdateLoanEquipmentStatus() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("equipmentStatus", "Dañado");
        loanArticleService.updateLoan("loan1", updates);
        assertEquals("Dañado", loanArticle.getEquipmentStatus());
        verify(articleRepository).saveAll(anyList());
    }

    @Test
    void testUpdateLoanInvalidFieldThrows() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, Object> updates = new HashMap<>();
        updates.put("invalido", "valor");
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.updateLoan("loan1", updates));
    }

    @Test
    void testUpdateLoanInvalidArticleStatusThrows() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> articleStates = new HashMap<>();
        articleStates.put("1", "Invalido");
        updates.put("articulo_estado", articleStates);
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.updateLoan("loan1", updates));
    }

    @Test
    void testUpdateLoanStatusDevuelto() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Devuelto");
        loanArticleService.updateLoan("loan1", updates);
        verify(loanArcticleRepository, atLeastOnce()).save(any());
    }

    @Test
    void testUpdateLoanStatusVencido() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Vencido");
        loanArticleService.updateLoan("loan1", updates);
        verify(loanArcticleRepository, atLeastOnce()).save(any());
    }

    @Test
    void testUpdateLoanStatusInvalidThrows() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Invalido");
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.updateLoan("loan1", updates));
    }

    @Test
    void testDetermineArticleStatus() {
        assertEquals("Disponible", loanArticleService.determineArticleStatus(null));
        assertEquals("Dañado", loanArticleService.determineArticleStatus("Dañado"));
        assertEquals("RequiereMantenimiento", loanArticleService.determineArticleStatus("Requiere mantenimiento"));
        assertEquals("Disponible", loanArticleService.determineArticleStatus("Otro"));
    }

    @Test
    void testEnviarRecordatoriosDevolucion() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findByLoanStatusAndDevolutionDate(anyString(), any(LocalDate.class))).thenReturn(List.of(loanArticle));
        when(alertRepository.save(any(Alert.class))).thenReturn(null);
        loanArticleService.enviarRecordatoriosDevolucion();
        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
    }

    @Test
    void testVerificarPrestamosVencidos() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findByLoanStatusAndDevolutionDateBefore(anyString(), any(LocalDate.class))).thenReturn(List.of(loanArticle));
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        loanArticleService.verificarPrestamosVencidos();
        verify(loanArcticleRepository, atLeastOnce()).save(any());
        verify(alertRepository, atLeastOnce()).save(any());
    }

    @Test
    void testMarkAsVencido() {
        LoanArticle loanArticle = createValidLoan();
        when(articleRepository.findAllById(any())).thenReturn(List.of(createAvailableArticle()));
        loanArticleService.markAsVencido(loanArticle);
        assertEquals("Vencido", loanArticle.getLoanStatus());
        verify(loanArcticleRepository).save(loanArticle);
        verify(alertRepository).save(any());
    }

    @Test
    void testParseDateString() {
        LocalDate date = loanArticleService.parseDate(LocalDate.now().toString());
        assertEquals(LocalDate.now(), date);
    }

    @Test
    void testParseDateLocalDate() {
        LocalDate now = LocalDate.now();
        assertEquals(now, loanArticleService.parseDate(now));
    }

    @Test
    void testParseDateInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.parseDate(1));
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.parseDate(null));
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.parseDate("no-date"));
    }

    @Test
    void testUpdateArticlesStatus() {
        LoanArticle loanArticle = createValidLoan();
        Article article = createAvailableArticle();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        when(articleRepository.findById(1)).thenReturn(Optional.of(article));
        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("1", "Prestado");
        loanArticleService.updateArticlesStatus("loan1", articulosUpdate);
        verify(articleRepository).save(article);
    }

    @Test
    void testUpdateArticlesStatusWithInvalidIdThrows() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setArticleIds(List.of(1));
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("2", "Prestado"); // not in loan
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.updateArticlesStatus("loan1", articulosUpdate));
    }

    @Test
    void testUpdateArticlesStatusWithInvalidStatusThrows() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("1", "NO_VALIDO");
        assertThrows(IllegalArgumentException.class, () -> loanArticleService.updateArticlesStatus("loan1", articulosUpdate));
    }

    @Test
    void testGetLoansByStatus() {
        when(loanArcticleRepository.findByLoanStatus("Prestado")).thenReturn(List.of(createValidLoan()));
        List<LoanArticle> loanArticles = loanArticleService.getLoans("Prestado");
        assertFalse(loanArticles.isEmpty());
    }

    @Test
    void testGetLoansByUnknownStatusReturnsAll() {
        when(loanArcticleRepository.findAll()).thenReturn(List.of(createValidLoan()));
        List<LoanArticle> loanArticles = loanArticleService.getLoans("Desconocido");
        assertFalse(loanArticles.isEmpty());
    }

    @Test
    void testGetLoanByIdSuccess() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.of(loanArticle));
        assertEquals(loanArticle, loanArticleService.getLoanById("loan1"));
    }

    @Test
    void testGetLoanByIdThrows() {
        when(loanArcticleRepository.findById("loan1")).thenReturn(Optional.empty());
        assertThrows(LoanException.LoanExceptionPrestamoIdNotFound.class, () -> loanArticleService.getLoanById("loan1"));
    }

    @Test
    void testGetLoansByUserSuccess() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findByUserId("uid")).thenReturn(List.of(loanArticle));
        List<LoanArticle> loanArticles = loanArticleService.getLoansByUser("uid");
        assertEquals(1, loanArticles.size());
    }

    @Test
    void testGetLoansByUserThrows() {
        when(loanArcticleRepository.findByUserId("uid")).thenReturn(Collections.emptyList());
        assertThrows(LoanException.LoanExceptionEstudianteHasNotPrestamo.class, () -> loanArticleService.getLoansByUser("uid"));
    }

    @Test
    void testGetAvailableArticlesInInterval() {
    }

    @Test
    void testGetAvailableArticlesInIntervalWithUnavailableArticles() {
        LoanArticle loanArticle = createValidLoan();
        loanArticle.setArticleIds(List.of(1,2));
        when(loanArcticleRepository.findOverlappingLoans(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(loanArticle));
        when(articleRepository.findByArticleStatusAndIdNotIn(anyString(), anySet()))
                .thenReturn(List.of(createAvailableArticle()));
        Object result = loanArticleService.getAvailableArticlesInInterval(LocalDate.now(), LocalDate.now().plusDays(1));
        assertNotNull(result);
    }

    @Test
    void testGetAvailableArticlesInIntervalInvalidDatesThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                loanArticleService.getAvailableArticlesInInterval(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () ->
                loanArticleService.getAvailableArticlesInInterval(LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class, () ->
                loanArticleService.getAvailableArticlesInInterval(LocalDate.now().plusDays(1), LocalDate.now()));
    }

    @Test
    void testGetLoansByDateRangeAndStatus() {
        when(mongoTemplate.find(any(Query.class), eq(LoanArticle.class)))
                .thenReturn(List.of(createValidLoan()));
        List<LoanArticle> loanArticles = loanArticleService.getLoansByDateRangeAndStatus(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "Prestado");
        assertFalse(loanArticles.isEmpty());
    }

    @Test
    void testGetLoansByDateRangeAndStatusThrows() {
        when(mongoTemplate.find(any(Query.class), eq(LoanArticle.class)))
                .thenThrow(new RuntimeException("error"));
        assertThrows(LoanException.class,
                () -> loanArticleService.getLoansByDateRangeAndStatus(LocalDate.now(), LocalDate.now(), "Prestado"));
    }

    @Test
    void testGetLoansByUserReport() {
        LoanArticle loanArticle = createValidLoan();
        when(loanArcticleRepository.findByUserId("uid")).thenReturn(List.of(loanArticle));
        List<LoanArticle> loanArticles = loanArticleService.getLoansByUserReport("uid");
        assertEquals(1, loanArticles.size());
    }
}