package edu.eci.cvds.proyect.coliseum.persistency.controller;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private LoanController controller;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        controller = new LoanController(loanService, alertRepository, articleRepository);
    }

    @Test
    void testGetLoans_AllLoans() {
        List<Loan> loans = List.of(new Loan(), new Loan());
        when(loanService.getLoans(null)).thenReturn(loans);

        ResponseEntity<?> response = controller.getLoans(null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(2, body.get("cantidad"));
        assertEquals(loans, body.get("prestamos"));
    }

    @Test
    void testGetLoans_ById() {
        Loan loan = new Loan();
        when(loanService.getLoanById("foo")).thenReturn(loan);

        ResponseEntity<?> response = controller.getLoans("LN-foo");
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(1, body.get("cantidad"));
        assertEquals(List.of(loan), body.get("prestamos"));
    }

    @Test
    void testGetLoans_ByUserId() {
        List<Loan> loans = List.of(new Loan());
        when(loanService.getLoansByUserReport("U-1")).thenReturn(loans);

        ResponseEntity<?> response = controller.getLoans("U-1");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByDateRange() {
        List<Loan> loans = List.of(new Loan());
        when(loanService.getLoansByDateRangeAndStatus(any(), any(), isNull())).thenReturn(loans);

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
    void testGetLoans_ByTipoEquipo_ArticlesNotFound() {
        when(articleRepository.findByName("Balon")).thenReturn(Optional.empty());
        when(loanService.getLoans(null)).thenReturn(List.of());

        ResponseEntity<?> response = controller.getLoans("tipo:Balon");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(0, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByTipoEquipo_WithArticles() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(List.of(article)));

        Loan loan = mock(Loan.class);
        when(loan.getArticleIds()).thenReturn(List.of(1));
        when(loanService.getLoans(null)).thenReturn(List.of(loan));

        ResponseEntity<?> response = controller.getLoans("tipo:Balon");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByHoras() {
        Loan hourlyLoan = new Loan();
        hourlyLoan.setLoanDescriptionType("[Préstamo por horas: 10:00-12:00] xyz");
        Loan dailyLoan = new Loan();
        dailyLoan.setLoanDescriptionType("Préstamo normal");
        when(loanService.getLoans(null)).thenReturn(List.of(hourlyLoan, dailyLoan));

        ResponseEntity<?> response = controller.getLoans("horas:true");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByEstado() {
        List<Loan> loans = List.of(new Loan());
        when(loanService.getLoans("Prestado")).thenReturn(loans);

        ResponseEntity<?> response = controller.getLoans("Prestado");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_ByNameUser() {
        Loan loan = new Loan();
        loan.setNameUser("Juan Pérez");
        when(loanService.getLoans(null)).thenReturn(List.of(loan));

        ResponseEntity<?> response = controller.getLoans("Juan");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("cantidad"));
    }

    @Test
    void testGetLoans_LoanException() {
        when(loanService.getLoans(null)).thenThrow(new LoanException("msg"));
        ResponseEntity<?> response = controller.getLoans(null);

        assertEquals(404, response.getStatusCodeValue());
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertTrue(body.get("Message").contains("msg"));
    }

    @Test
    void testSave_NormalLoan() {
        Loan loan = new Loan();
        loan.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanService.createLoan(any())).thenReturn(loan);

        ResponseEntity<Object> response = controller.save(loan, false, null, null);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(loan, response.getBody());
    }

    @Test
    void testSave_HourlyLoan() {
        Loan loan = new Loan();
        loan.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanService.createLoan(any())).thenReturn(loan);

        ResponseEntity<Object> response = controller.save(loan, true, "08:00", "10:00");
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(loan, response.getBody());
    }

    @Test
    void testSave_ArticleNotAvailable() {
        Loan loan = new Loan();
        loan.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Prestado");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));

        ResponseEntity<Object> response = controller.save(loan, false, null, null);
        assertEquals(500, response.getStatusCodeValue()); // <-- Espera 500, no 400
    }

    @Test
    void testSave_InvalidTimeFormat() {
        Loan loan = new Loan();
        loan.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));

        ResponseEntity<Object> response = controller.save(loan, true, "bad", "10:00");
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testSave_InternalError() {
        Loan loan = new Loan();
        loan.setArticleIds(List.of(1));
        Article article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");
        when(articleRepository.findAllById(List.of(1))).thenReturn(List.of(article));
        when(loanService.createLoan(any())).thenThrow(new RuntimeException("fail"));

        ResponseEntity<Object> response = controller.save(loan, false, null, null);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testGenerateReport_Student() {
        List<Loan> loans = List.of(new Loan());
        when(loanService.getLoansByUserReport("U-1")).thenReturn(loans);

        ResponseEntity<?> response = controller.generateReport("student", "U-1", null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_Equipment() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(List.of(article)));
        Loan loan = mock(Loan.class);
        when(loan.getArticleIds()).thenReturn(List.of(1));
        when(loanService.getLoans(null)).thenReturn(List.of(loan));

        ResponseEntity<?> response = controller.generateReport("equipment", "Balon", null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_Status() {
        List<Loan> loans = List.of(new Loan());
        when(loanService.getLoans("Prestado")).thenReturn(loans);

        ResponseEntity<?> response = controller.generateReport("status", "Prestado", null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_Hourly() {
        Loan loan = new Loan();
        loan.setLoanDescriptionType("[Préstamo por horas: 10:00-12:00] xyz");
        when(loanService.getLoans(null)).thenReturn(List.of(loan));

        ResponseEntity<?> response = controller.generateReport("hourly", null, null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
    }

    @Test
    void testGenerateReport_DateRange() {
        List<Loan> loans = List.of(new Loan());
        when(loanService.getLoansByDateRangeAndStatus(any(), any(), isNull())).thenReturn(loans);

        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        ResponseEntity<?> response = controller.generateReport("daterange", null, start, end);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1, body.get("totalItems"));
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

        Loan loan = new Loan();
        when(loanService.getLoanById("LN-1")).thenReturn(loan);

        ResponseEntity<Object> response = controller.update("LN-1", updates, null, null, null);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(loan, response.getBody());
        verify(loanService).devolverLoan("LN-1");
    }

    @Test
    void testUpdate_HourlyLoanUpdate() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("loanDescriptionType", "[Préstamo por horas: 08:00-10:00] desc");

        Loan loan = new Loan();
        loan.setLoanDescriptionType("[Préstamo por horas: 08:00-10:00] desc");
        loan.setLoanDate(LocalDate.now());
        when(loanService.getLoanById("LN-1")).thenReturn(loan);

        ResponseEntity<Object> response = controller.update("LN-1", updates, true, "09:00", "11:00");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testUpdate_ArticulosUpdate() {
        Map<String, Object> updates = new HashMap<>();
        Map<String, String> articulos = Map.of("1", "Dañado");
        updates.put("articulos", articulos);

        Loan loan = new Loan();
        when(loanService.getLoanById("LN-1")).thenReturn(loan);

        ResponseEntity<Object> response = controller.update("LN-1", updates, null, null, null);
        assertEquals(200, response.getStatusCodeValue());
        verify(loanService).updateArticlesStatus(eq("LN-1"), any());
    }

    @Test
    void testUpdate_Exception() {
        Map<String, Object> updates = new HashMap<>();
        when(loanService.getLoanById("LN-1")).thenThrow(new LoanException("fail"));

        ResponseEntity<Object> response = controller.update("LN-1", updates, null, null, null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDelete_Success() {

        ResponseEntity<Object> response = controller.delete("LN-1");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDelete_LoanException() {
        doThrow(new LoanException("fail")).when(loanService).deleteLoanById("LN-1");

        ResponseEntity<Object> response = controller.delete("LN-1");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDelete_RuntimeException() {
        doThrow(new RuntimeException("fail")).when(loanService).deleteLoanById("LN-1");

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