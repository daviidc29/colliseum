package edu.eci.cvds.proyect.coliseum.persistency.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.ArticleLoanStats;
import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleLoanStatsRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleFileGenerationService;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleService;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private ArticleController articleController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetArticles_NoQuery() {
        // Arrange
        List<Article> articles = Arrays.asList(
                new Article(1, "Balon", "Disponible", "Desc1", "/images/balon.png"),
                new Article(2, "Raqueta", "Disponible", "Desc2", "/images/raqueta.png")
        );
        when(articleService.getAll()).thenReturn(articles);

        // Act
        ResponseEntity<?> response = articleController.getArticles(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(2, body.get("cantidad"));
        verify(articleService, times(1)).getAll();
    }

    @Test
    void testGetArticles_NumericQuery() {
        // Arrange
        String q = "1";
        Article article = new Article(1, "Balon", "Disponible", "Desc1", "/images/balon.png");
        when(articleService.getOne(1)).thenReturn(article);

        // Act
        ResponseEntity<?> response = articleController.getArticles(q);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        List<?> resultList = (List<?>) body.get("articulos");
        assertEquals(1, resultList.size());
        verify(articleService, times(1)).getOne(1);
    }

    @Test
    void testGetArticles_DisponiblesQuery() {
        // Arrange
        String q = "disponibles:Balon";
        Article art1 = new Article(1, "Balon", "Disponible", "Desc1", "/images/balon.png");
        Article art2 = new Article(2, "Balon", "Dañado", "Desc2", "/images/balon2.png");
        when(articleService.getArticlesNames("Balon")).thenReturn(Arrays.asList(art1, art2));

        // Act
        ResponseEntity<?> response = articleController.getArticles(q);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        List<?> resultList = (List<?>) body.get("articulos");
        // Filtering in the code leaves only "Disponible"
        assertEquals(1, resultList.size());
        verify(articleService, times(1)).getArticlesNames("Balon");
    }

    @Test
    void testGetArticles_StatusQuery() {
        // Arrange
        String q = "Disponible";
        Article art1 = new Article(1, "Balon", "Disponible", "Desc1", "/images/balon.png");
        Article art2 = new Article(2, "Raqueta", "Disponible", "Desc2", "/images/raqueta.png");
        when(articleService.getArticlesStatus("Disponible"))
                .thenReturn(Arrays.asList(art1, art2));

        // Act
        ResponseEntity<?> response = articleController.getArticles(q);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        List<?> resultList = (List<?>) body.get("articulos");
        assertEquals(2, resultList.size());
        verify(articleService, times(1)).getArticlesStatus("Disponible");
    }

    @Test
    void testGetArticles_NameQuery() {
        // Arrange
        String q = "Raqueta";
        Article art = new Article(2, "Raqueta", "Disponible", "Desc2", "/images/raqueta.png");
        when(articleService.getArticlesNames("Raqueta"))
                .thenReturn(Collections.singletonList(art));

        // Act
        ResponseEntity<?> response = articleController.getArticles(q);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        @SuppressWarnings("unchecked")
        List<Article> resultList = (List<Article>) body.get("articulos");
        assertEquals(1, resultList.size());
        verify(articleService, times(1)).getArticlesNames("Raqueta");
    }

    @Test
    void testGetArticles_Exception() {
        // Arrange
        String q = "someQuery";
        when(articleService.getArticlesNames("someQuery"))
                .thenThrow(new RuntimeException("Simulated exception"));

        // Act
        ResponseEntity<?> response = articleController.getArticles(q);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Error al obtener artículos", body.get("error"));
        verify(articleService, times(1)).getArticlesNames("someQuery");
    }

    @Test
    void testSave_Success() {
        // Arrange
        ArticleDto dto = ArticleDto.builder()
                .name("Balon")
                .articleStatus("Disponible")
                .description("Desc")
                .build();
        Article expectedArticle = new Article(1, "Balon", "Disponible", "Desc", "/images/balon.png");
        when(articleService.save(dto)).thenReturn(expectedArticle);

        // Act
        ResponseEntity<Object> response = articleController.save(dto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        Article savedArticle = (Article) response.getBody();
        assertEquals(expectedArticle, savedArticle);
        verify(articleService, times(1)).save(dto);
    }

    @Test
    void testSave_Exception() {
        // Arrange
        ArticleDto dto = new ArticleDto();
        when(articleService.save(dto)).thenThrow(new RuntimeException("Simulated save error"));

        // Act
        ResponseEntity<Object> response = articleController.save(dto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Error al guardar el articulo", body.get("Error"));
        verify(articleService, times(1)).save(dto);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        Integer id = 1;
        ArticleDto dto = new ArticleDto();
        Article updatedArticle = new Article(1, "Balon", "Disponible", "Desc", "/images/balon.png");
        when(articleService.update(id, dto)).thenReturn(updatedArticle);

        // Act
        ResponseEntity<Object> response = articleController.update(id, dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Article resultArticle = (Article) response.getBody();
        assertNotNull(resultArticle);
        assertEquals(updatedArticle, resultArticle);
        verify(articleService, times(1)).update(id, dto);
    }

    @Test
    void testUpdate_Exception() {
        // Arrange
        Integer id = 99;
        ArticleDto dto = new ArticleDto();
        when(articleService.update(id, dto)).thenThrow(new RuntimeException("Simulated update error"));

        // Act
        ResponseEntity<Object> response = articleController.update(id, dto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Error al actualizar el articulo", body.get("Error"));
        verify(articleService, times(1)).update(id, dto);
    }

    @Test
    void testDelete_Success() {
        // Arrange
        Integer id = 1;
        Article deletedArticle = new Article(1, "Balon", "Disponible", "Desc", "/images/balon.png");
        when(articleService.delete(id)).thenReturn(deletedArticle);

        // Act
        ResponseEntity<Object> response = articleController.delete(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Articulo eliminado correctamente", body.get("message"));
        verify(articleService, times(1)).delete(id);
    }

    @Test
    void testDelete_Exception() {
        // Arrange
        Integer id = 99;
        when(articleService.delete(id)).thenThrow(new RuntimeException("Simulated delete error"));

        // Act
        ResponseEntity<Object> response = articleController.delete(id);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Error al eliminar el articulo", body.get("Error"));
        verify(articleService, times(1)).delete(id);
    }

    @Test
    @SuppressWarnings("unchecked") // Supresion segura del cast controlado
    void testGetAllAlerts_Success() {
        // Arrange
        Alert alert = new Alert("id1", "Balon", "Mensaje", LocalDateTime.now());
        when(alertRepository.findAll()).thenReturn(Collections.singletonList(alert));

        // Act
        ResponseEntity<?> response = articleController.getAllAlerts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Object body = response.getBody();
        assertNotNull(body);
        assertTrue(body instanceof List<?>);

        List<Alert> alerts = (List<Alert>) body;
        assertEquals(1, alerts.size());
        assertEquals("id1", alerts.get(0).getId());

        verify(alertRepository, times(1)).findAll();
    }


    @Test
    void testGetAllAlerts_Exception() {
        // Arrange
        when(alertRepository.findAll()).thenThrow(new RuntimeException("Simulated alert error"));

        // Act
        ResponseEntity<?> response = articleController.getAllAlerts();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Error al obtener las alertas", body.get("Error "));
        verify(alertRepository, times(1)).findAll();
    }

    @Test
    void testGetArticleLoanStats_Success() throws IOException {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        // Crear datos de prueba
        List<Article> articles = Arrays.asList(
                new Article(1, "Balon", "Disponible", "Desc1", "/images/balon.png"),
                new Article(2, "Raqueta", "Disponible", "Desc2", "/images/raqueta.png")
        );

        // Crear préstamos con referencias a los artículos
        List<LoanArticle> loans = new ArrayList<>();
        LoanArticle loan1 = new LoanArticle();
        loan1.setId("loan1");
        loan1.setArticleIds(List.of(1, 2));
        loans.add(loan1);

        LoanArticle loan2 = new LoanArticle();
        loan2.setId("loan2");
        loan2.setArticleIds(List.of(1));
        loans.add(loan2);

        // Configurar mocks
        when(articleService.getAll()).thenReturn(articles);
        when(loanArticleService.getLoans(null)).thenReturn(loans);

        // Capturar el objeto ArticleLoanStats que se pasa al file generation service
        ArgumentCaptor<ArticleLoanStats> statsCaptor = ArgumentCaptor.forClass(ArticleLoanStats.class);
        when(fileGenerationService.generateArticleStatsPdf(statsCaptor.capture())).thenReturn(new byte[]{1, 2, 3});
        when(fileGenerationService.generateArticleStatsExcel(any(ArticleLoanStats.class))).thenReturn(new byte[]{4, 5, 6});

        // Capturar el objeto que se guarda en el repositorio
        ArgumentCaptor<ArticleLoanStats> savedStatsCaptor = ArgumentCaptor.forClass(ArticleLoanStats.class);
        when(statsRepository.save(savedStatsCaptor.capture())).thenAnswer(invocation -> {
            ArticleLoanStats stats = invocation.getArgument(0);
            stats.setId("report123");
            return stats;
        });

        // Act
        ResponseEntity<?> response = controller.getArticleLoanStats("testuser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(2, body.get("totalArticulos"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stats = (List<Map<String, Object>>) body.get("estadisticas");
        assertEquals(2, stats.size());

        // Verificar que el primer artículo (más prestado) tiene 2 préstamos
        assertEquals(1, stats.get(0).get("id"));
        assertEquals("Balon", stats.get(0).get("name"));
        assertEquals(2L, stats.get(0).get("vecesPrestado"));

        // Verificar que el segundo artículo tiene 1 préstamo
        assertEquals(2, stats.get(1).get("id"));
        assertEquals("Raqueta", stats.get(1).get("name"));
        assertEquals(1L, stats.get(1).get("vecesPrestado"));

        // Verificar que se crearon los enlaces de descarga
        @SuppressWarnings("unchecked")
        Map<String, String> links = (Map<String, String>) body.get("downloadLinks");
        assertEquals("/Article/stats/pdf/report123", links.get("pdf"));
        assertEquals("/Article/stats/excel/report123", links.get("excel"));

        // Verificar las interacciones con los servicios
        verify(fileGenerationService).generateArticleStatsPdf(any(ArticleLoanStats.class));
        verify(fileGenerationService).generateArticleStatsExcel(any(ArticleLoanStats.class));
        verify(statsRepository).save(any(ArticleLoanStats.class));

        // Verificar que se usó el nombre de usuario proporcionado
        assertEquals("testuser", savedStatsCaptor.getValue().getGeneratedBy());
    }

    @Test
    void testGetArticleLoanStats_DefaultUsername() throws IOException {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        when(articleService.getAll()).thenReturn(Collections.emptyList());
        when(loanArticleService.getLoans(null)).thenReturn(Collections.emptyList());
        when(fileGenerationService.generateArticleStatsPdf(any())).thenReturn(new byte[]{1, 2, 3});
        when(fileGenerationService.generateArticleStatsExcel(any())).thenReturn(new byte[]{4, 5, 6});

        ArgumentCaptor<ArticleLoanStats> statsCaptor = ArgumentCaptor.forClass(ArticleLoanStats.class);
        when(statsRepository.save(statsCaptor.capture())).thenAnswer(invocation -> {
            ArticleLoanStats stats = invocation.getArgument(0);
            stats.setId("defaultReport");
            return stats;
        });

        // Act - llamar sin nombre de usuario
        ResponseEntity<?> response = controller.getArticleLoanStats(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Juan-cely-l", statsCaptor.getValue().getGeneratedBy());
    }

    @Test
    void testGetArticleLoanStats_EmptyArticleList() throws IOException {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        when(articleService.getAll()).thenReturn(Collections.emptyList());
        when(loanArticleService.getLoans(null)).thenReturn(Collections.emptyList());
        when(fileGenerationService.generateArticleStatsPdf(any())).thenReturn(new byte[]{1, 2, 3});
        when(fileGenerationService.generateArticleStatsExcel(any())).thenReturn(new byte[]{4, 5, 6});

        when(statsRepository.save(any())).thenAnswer(invocation -> {
            ArticleLoanStats stats = invocation.getArgument(0);
            stats.setId("emptyReport");
            return stats;
        });

        // Act
        ResponseEntity<?> response = controller.getArticleLoanStats("testuser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(0, body.get("totalArticulos"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stats = (List<Map<String, Object>>) body.get("estadisticas");
        assertTrue(stats.isEmpty());
    }

    @Test
    void testGetArticleLoanStats_GenerationException() throws IOException {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        List<Article> articles = Arrays.asList(new Article(1, "Balon", "Disponible", "Desc1", "/images/balon.png"));
        when(articleService.getAll()).thenReturn(articles);
        when(loanArticleService.getLoans(null)).thenReturn(Collections.emptyList());

        // Simular error en generación de PDF
        when(fileGenerationService.generateArticleStatsPdf(any())).thenThrow(new IOException("Error PDF"));

        // Act
        ResponseEntity<?> response = controller.getArticleLoanStats("testuser");

        // Assert - el endpoint debería continuar y devolver la respuesta JSON
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.get("totalArticulos"));

        // No debería haberse guardado ningún reporte
        verify(statsRepository, never()).save(any());
    }

    @Test
    void testGetArticleLoanStats_Exception() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        when(articleService.getAll()).thenThrow(new RuntimeException("Test exception"));

        // Act
        ResponseEntity<?> response = controller.getArticleLoanStats("testuser");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Error al obtener estadísticas de préstamos", body.get("Error"));
    }

    @Test
    void testGetStatsPdf_Success() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        String reportId = "report123";
        byte[] pdfBytes = {1, 2, 3, 4, 5};

        ArticleLoanStats stats = new ArticleLoanStats();
        stats.setId(reportId);
        stats.setPdfFile(pdfBytes);

        when(statsRepository.findById(reportId)).thenReturn(Optional.of(stats));

        // Act
        ResponseEntity<byte[]> response = controller.getStatsPdf(reportId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=articulos_prestamos_stats_" + reportId + ".pdf",
                response.getHeaders().getFirst("Content-Disposition"));
        assertArrayEquals(pdfBytes, response.getBody());
    }

    @Test
    void testGetStatsPdf_NotFound() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        String reportId = "nonexistent";
        when(statsRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = controller.getStatsPdf(reportId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetStatsPdf_EmptyPdfFile() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        String reportId = "report123";

        ArticleLoanStats stats = new ArticleLoanStats();
        stats.setId(reportId);
        stats.setPdfFile(new byte[0]); // Archivo vacío

        when(statsRepository.findById(reportId)).thenReturn(Optional.of(stats));

        // Act
        ResponseEntity<byte[]> response = controller.getStatsPdf(reportId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetStatsExcel_Success() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        String reportId = "report123";
        byte[] excelBytes = {1, 2, 3, 4, 5};

        ArticleLoanStats stats = new ArticleLoanStats();
        stats.setId(reportId);
        stats.setExcelFile(excelBytes);

        when(statsRepository.findById(reportId)).thenReturn(Optional.of(stats));

        // Act
        ResponseEntity<byte[]> response = controller.getStatsExcel(reportId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                response.getHeaders().getContentType());
        assertEquals("attachment; filename=articulos_prestamos_stats_" + reportId + ".xlsx",
                response.getHeaders().getFirst("Content-Disposition"));
        assertArrayEquals(excelBytes, response.getBody());
    }

    @Test
    void testGetStatsExcel_NotFound() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        String reportId = "nonexistent";
        when(statsRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = controller.getStatsExcel(reportId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetStatsExcel_EmptyExcelFile() {
        // Arrange
        ArticleService articleService = mock(ArticleService.class);
        AlertRepository alertRepository = mock(AlertRepository.class);
        LoanArticleService loanArticleService = mock(LoanArticleService.class);
        ArticleFileGenerationService fileGenerationService = mock(ArticleFileGenerationService.class);
        ArticleLoanStatsRepository statsRepository = mock(ArticleLoanStatsRepository.class);

        ArticleController controller = new ArticleController(
                articleService, alertRepository, loanArticleService,
                fileGenerationService, statsRepository);

        String reportId = "report123";

        ArticleLoanStats stats = new ArticleLoanStats();
        stats.setId(reportId);
        stats.setExcelFile(new byte[0]); // Archivo vacío

        when(statsRepository.findById(reportId)).thenReturn(Optional.of(stats));

        // Act
        ResponseEntity<byte[]> response = controller.getStatsExcel(reportId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }


}