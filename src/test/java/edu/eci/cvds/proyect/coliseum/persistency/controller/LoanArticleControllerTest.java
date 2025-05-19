package edu.eci.cvds.proyect.coliseum.persistency.controller;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanArticleControllerTest {

    @Mock
    private LoanArticleService loanArticleService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private LoanArticleController controller;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        controller = new LoanArticleController(loanArticleService, alertRepository, articleRepository);
    }

    @Test
    void testGetLoans_AllLoans() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle(), new LoanArticle());
        when(loanArticleService.getLoans(null)).thenReturn(loanArticles);

        ResponseEntity<?> response = controller.getLoans(null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(2, body.get("cantidad"));
        assertEquals(loanArticles, body.get("prestamos"));
    }

    @Test
    void testGetLoans_ById() {
        LoanArticle loanArticle = new LoanArticle();
        when(loanArticleService.getLoanById("foo")).thenReturn(loanArticle);

        ResponseEntity<?> response = controller.getLoans("LN-foo");
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(1, body.get("cantidad"));
        assertEquals(List.of(loanArticle), body.get("prestamos"));
    }

    @Test
    void testGetLoans_ByUserId() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle());
        when(loanArticleService.getLoansByUserReport("U-1")).thenReturn(loanArticles);

        ResponseEntity<?> response = controller.getLoans("U-1");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByDateRange() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle());
        when(loanArticleService.getLoansByDateRangeAndStatus(any(), any(), isNull())).thenReturn(loanArticles);

        ResponseEntity<?> response = controller.getLoans("fechas:2024-01-01:2024-02-01");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByDateRange_Error() {
        ResponseEntity<?> response = controller.getLoans("fechas:2024-01-01");
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertTrue(error.get("Message").contains("Formato de fechas inválido"));
    }

    @Test
    void testGetLoans_ByRangoHoras() {

    }

    @Test
    void testGetLoans_ByRangoHoras_SimplifiedFormat() {
        // Create a loan article with specific time range
        LoanArticle loanArticle = new LoanArticle();
        loanArticle.setStartTime(LocalTime.of(14, 0));
        loanArticle.setEndTime(LocalTime.of(16, 0));

        when(loanArticleService.getLoans(null)).thenReturn(List.of(loanArticle));

        ResponseEntity<?> response = controller.getLoans("rangohoras:14:16");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByRangoHoras_InvalidFormat() {
        ResponseEntity<?> response = controller.getLoans("rangohoras:14");
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertTrue(error.get("Message").contains("Formato de horas inválido"));
    }

    @Test
    void testGetLoans_ByTipoEquipo_ArticlesNotFound() {
        when(articleRepository.findByName("Balon")).thenReturn(Optional.empty());
        when(loanArticleService.getLoans(null)).thenReturn(List.of());

        ResponseEntity<?> response = controller.getLoans("tipo:Balon");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(0, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByTipoEquipo_WithArticles() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(List.of(article)));

        LoanArticle loanArticle = mock(LoanArticle.class);
        when(loanArticle.getArticleIds()).thenReturn(List.of(1));
        when(loanArticleService.getLoans(null)).thenReturn(List.of(loanArticle));

        ResponseEntity<?> response = controller.getLoans("tipo:Balon");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByHoras() {
        LoanArticle hourlyLoanArticle = new LoanArticle();
        hourlyLoanArticle.setLoanDescriptionType("[Préstamo por horas: 10:00-12:00] xyz");
        LoanArticle dailyLoanArticle = new LoanArticle();
        dailyLoanArticle.setLoanDescriptionType("Préstamo normal");
        when(loanArticleService.getLoans(null)).thenReturn(List.of(hourlyLoanArticle, dailyLoanArticle));

        ResponseEntity<?> response = controller.getLoans("horas:true");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByEstado() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle());
        when(loanArticleService.getLoans("Prestado")).thenReturn(loanArticles);

        ResponseEntity<?> response = controller.getLoans("Prestado");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByNameUser() {
        LoanArticle loanArticle = new LoanArticle();
        loanArticle.setNameUser("Juan Pérez");
        when(loanArticleService.getLoans(null)).thenReturn(List.of(loanArticle));

        ResponseEntity<?> response = controller.getLoans("Juan");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_LoanException() {
        when(loanArticleService.getLoans(null)).thenThrow(new LoanException("msg"));
        ResponseEntity<?> response = controller.getLoans(null);

        assertEquals(404, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("Message").contains("msg"));
    }

    @Test
    void testSave_NormalLoan() {
        LoanArticle loanArticle = new LoanArticle();
        loanArticle.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanArticleService.createLoan(any())).thenReturn(loanArticle);

        ResponseEntity<Object> response = controller.save(loanArticle, "14:00", "16:00");
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(loanArticle, response.getBody());
    }

    @Test
    void testSave_ArticleNotAvailable() {

    }

    @Test
    void testSave_InvalidTimeFormat() {

    }

    @Test
    void testSave_InvalidTimeRange() {

    }

    @Test
    void testSave_NoArticles() {

    }

    @Test
    void testSave_InternalError() {
        LoanArticle loanArticle = new LoanArticle();
        loanArticle.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanArticleService.createLoan(any())).thenThrow(new RuntimeException("fail"));

        ResponseEntity<Object> response = controller.save(loanArticle, "14:00", "16:00");
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testGenerateReport_Student() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle());
        when(loanArticleService.getLoansByUserReport("U-1")).thenReturn(loanArticles);

        ResponseEntity<?> response = controller.generateReport("student", "U-1", null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_Student_MissingValue() {
        ResponseEntity<?> response = controller.generateReport("student", null, null, null);
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("Message").contains("Debe proporcionar un ID de estudiante"));
    }

    @Test
    void testGenerateReport_Equipment() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(List.of(article)));
        LoanArticle loanArticle = mock(LoanArticle.class);
        when(loanArticle.getArticleIds()).thenReturn(List.of(1));
        when(loanArticleService.getLoans(null)).thenReturn(List.of(loanArticle));

        ResponseEntity<?> response = controller.generateReport("equipment", "Balon", null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_Status() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle());
        when(loanArticleService.getLoans("Prestado")).thenReturn(loanArticles);

        ResponseEntity<?> response = controller.generateReport("status", "Prestado", null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_Hourly() {
        LoanArticle loanArticle = new LoanArticle();
        loanArticle.setLoanDescriptionType("[Préstamo por horas: 10:00-12:00] xyz");
        when(loanArticleService.getLoans(null)).thenReturn(List.of(loanArticle));

        ResponseEntity<?> response = controller.generateReport("hourly", null, null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_DateRange() {
        List<LoanArticle> loanArticles = List.of(new LoanArticle());
        when(loanArticleService.getLoansByDateRangeAndStatus(any(), any(), isNull())).thenReturn(loanArticles);

        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        ResponseEntity<?> response = controller.generateReport("daterange", null, start, end);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_DateRange_MissingDates() {
        ResponseEntity<?> response = controller.generateReport("daterange", null, null, null);
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("Message").contains("Debe proporcionar fechas de inicio y fin"));
    }

    @Test
    void testGenerateReport_InvalidType() {
        ResponseEntity<?> response = controller.generateReport("invalid", null, null, null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testUpdate_DevolverLoan() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("devolver", true);

        LoanArticle loanArticle = new LoanArticle();
        when(loanArticleService.getLoanById("LN-1")).thenReturn(loanArticle);

        ResponseEntity<Object> response = controller.update("LN-1", updates, null, null);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(loanArticle, response.getBody());
        verify(loanArticleService).devolverLoan("LN-1");
    }

    @Test
    void testUpdate_HourlyLoanUpdate() {

    }

    @Test
    void testUpdate_InvalidTimeFormat() {
        Map<String, Object> updates = new HashMap<>();

        LoanArticle loanArticle = new LoanArticle();
        loanArticle.setLoanDescriptionType("[Préstamo por horas: 14:00-16:00] Clase deportiva");
        when(loanArticleService.getLoanById("LN-1")).thenReturn(loanArticle);

        ResponseEntity<Object> response = controller.update("LN-1", updates, "invalid", "17:30");
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("Message").contains("Formato de hora inválido"));
    }

    @Test
    void testUpdate_ArticulosUpdate() {
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> articulos = Map.of("1", "Dañado");
        updates.put("articulos", articulos);

        LoanArticle loanArticle = new LoanArticle();
        when(loanArticleService.getLoanById("LN-1")).thenReturn(loanArticle);

        ResponseEntity<Object> response = controller.update("LN-1", updates, null, null);
        assertEquals(200, response.getStatusCodeValue());
        verify(loanArticleService).updateArticlesStatus(eq("LN-1"), any());
    }

    @Test
    void testUpdate_Exception() {
        Map<String, Object> updates = new HashMap<>();
        when(loanArticleService.getLoanById("LN-1")).thenThrow(new LoanException("fail"));

        ResponseEntity<Object> response = controller.update("LN-1", updates, null, null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDelete_Success() {
        ResponseEntity<Object> response = controller.delete("LN-1");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDelete_LoanException() {
        doThrow(new LoanException("fail")).when(loanArticleService).deleteLoanById("LN-1");

        ResponseEntity<Object> response = controller.delete("LN-1");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDelete_RuntimeException() {
        doThrow(new RuntimeException("fail")).when(loanArticleService).deleteLoanById("LN-1");

        ResponseEntity<Object> response = controller.delete("LN-1");
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testGetLoanAlerts_All() {
        Alert alert1 = new Alert("id1", "desc", "Préstamo vencido", LocalDateTime.now());
        Alert alert2 = new Alert("id2", "desc", "Recordatorio: devolución", LocalDateTime.now());
        when(alertRepository.findAll()).thenReturn(List.of(alert1, alert2));

        ResponseEntity<?> response = controller.getLoanAlerts(null, null, 0, 10);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(2, body.get("totalAlertas"));
    }

    @Test
    void testGetLoanAlerts_ByUser() {
        Alert alert = new Alert("id1", "desc", "Préstamo vencido", LocalDateTime.now());
        when(alertRepository.findByMessageContainingIgnoreCase("U-1")).thenReturn(List.of(alert));

        ResponseEntity<?> response = controller.getLoanAlerts("U-1", null, 0, 10);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalAlertas"));
    }

    @Test
    void testGetLoanAlerts_FilterByDays() {
        Alert alert = new Alert("id1", "desc", "Préstamo vencido", LocalDateTime.now().minusDays(1));
        when(alertRepository.findAll()).thenReturn(List.of(alert));

        ResponseEntity<?> response = controller.getLoanAlerts(null, 2, 0, 10);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalAlertas"));
    }

    @Test
    void testGetLoanAlerts_InvalidParameters() {
        ResponseEntity<?> response = controller.getLoanAlerts(null, -1, 0, 10);
        assertEquals(400, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("Message").contains("Valor negativo no permitido"));
    }

    @Test
    void testGetLoanAlerts_Exception() {
        when(alertRepository.findAll()).thenThrow(new RuntimeException("fail"));

        ResponseEntity<?> response = controller.getLoanAlerts(null, null, 0, 10);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testExtractArticulosMap_valid() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", "Bueno");
        Map<String, String> result = controller.extractArticulosMap(map);
        assertEquals("Bueno", result.get("1"));
    }

    @Test
    void testExtractArticulosMap_invalidKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("abc", "Bueno");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.extractArticulosMap(map));
        assertTrue(ex.getMessage().contains("ID de artículo inválido"));
    }

    @Test
    void testExtractArticulosMap_invalidInstance() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.extractArticulosMap("bad"));
        assertTrue(ex.getMessage().contains("debe ser un objeto JSON válido"));
    }

    @Test
    void testExtractArticulosMap_null() {
        assertTrue(controller.extractArticulosMap(null).isEmpty());
    }
}