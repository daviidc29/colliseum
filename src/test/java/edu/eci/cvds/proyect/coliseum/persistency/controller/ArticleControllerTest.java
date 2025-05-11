package edu.eci.cvds.proyect.coliseum.persistency.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import edu.eci.cvds.proyect.coliseum.persistency.Controller.ArticleController;
import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
}