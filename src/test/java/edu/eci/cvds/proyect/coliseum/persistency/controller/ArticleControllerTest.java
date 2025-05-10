package edu.eci.cvds.proyect.coliseum.persistency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private AlertRepository alertRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    // Pruebas para getArticles
    @Test
    void testGetAllArticlesSuccess() throws Exception {
        List<Article> articles = Arrays.asList(new Article(1, "Laptop", "Disponible", "Desc", "img.png"));
        Mockito.when(articleService.getAll()).thenReturn(articles);

        mockMvc.perform(get("/Article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.articulos[0].name").value("Laptop"));
    }

    @Test
    void testGetArticlesByIdSuccess() throws Exception {
        Article article = new Article(1, "Laptop", "Disponible", "Desc", "img.png");
        Mockito.when(articleService.getOne(1)).thenReturn(article);

        mockMvc.perform(get("/Article?q=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.articulos[0].id").value(1));
    }

    @Test
    void testGetArticlesByDisponiblesSuccess() throws Exception {
        List<Article> articles = Arrays.asList(new Article(1, "Laptop", "Disponible", "Desc", "img.png"));
        Mockito.when(articleService.getArticlesNames("Laptop")).thenReturn(articles);

        mockMvc.perform(get("/Article?q=disponibles:Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.articulos[0].articleStatus").value("Disponible"));
    }

    @Test
    void testGetArticlesInternalError() throws Exception {
        Mockito.when(articleService.getAll()).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/Article"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error al obtener artículos"));
    }
    // Pruebas para consultar por estado

    @Test
    void testGetArticlesByStatusSuccess() throws Exception {
        List<Article> articles = Arrays.asList(
            new Article(1, "Laptop", "Disponible", "Descripción", "img.png"),
            new Article(2, "Mouse", "Disponible", "Descripción", "img.png")
        );
        Mockito.when(articleService.getArticlesStatus("Disponible")).thenReturn(articles);

        mockMvc.perform(get("/Article?q=Disponible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.articulos[0].articleStatus").value("Disponible"));
    }

    @Test
    void testGetArticlesByStatusFailure() throws Exception {
        Mockito.when(articleService.getArticlesStatus("Disponible"))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(get("/Article?q=Disponible"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error al obtener artículos"));
    }

    // Pruebas para consultar por nombre
    @Test
    void testGetArticlesByNameSuccess() throws Exception {
        List<Article> articles = Arrays.asList(
            new Article(1, "Laptop", "Disponible", "Descripción", "img.png"),
            new Article(3, "Laptop", "Prestado", "Descripción", "img.png")
        );
        Mockito.when(articleService.getArticlesNames("Laptop")).thenReturn(articles);

        mockMvc.perform(get("/Article?q=Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.articulos[0].name").value("Laptop"));
    }

    @Test
    void testGetArticlesByNameFailure() throws Exception {
        Mockito.when(articleService.getArticlesNames("Teclado"))
                .thenThrow(new RuntimeException("Artículo no encontrado"));

        mockMvc.perform(get("/Article?q=Teclado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error al obtener artículos"));
    }

    // Prueba para estado inválido (debe buscar por nombre)
    @Test
    void testGetArticlesByInvalidStatus() throws Exception {
        List<Article> articles = Arrays.asList(
            new Article(4, "Roto", "Dañado", "Descripción", "img.png")
        );
        Mockito.when(articleService.getArticlesNames("Roto")).thenReturn(articles);

        mockMvc.perform(get("/Article?q=Roto")) // "Roto" no es un estado válido
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articulos[0].name").value("Roto"));
    }

    // Pruebas para save
    @Test
    void testSaveArticleSuccess() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "Disponible", "Desc", null);
        Article saved = new Article(1, "Laptop", "Disponible", "Desc", "/images/laptop.png");
        Mockito.when(articleService.save(Mockito.any(ArticleDto.class))).thenReturn(saved);

        mockMvc.perform(post("/Article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testSaveArticleFailure() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "Disponible", "Desc", null);
        Mockito.when(articleService.save(Mockito.any())).thenThrow(new RuntimeException("Error guardando"));

        mockMvc.perform(post("/Article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al guardar el articulo"));
    }

    // Pruebas para update
    @Test
    void testUpdateArticleSuccess() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "Dañado", "Desc", null);
        Article updated = new Article(1, "Laptop", "Dañado", "Desc", "/images/laptop.png");
        Mockito.when(articleService.update(1, dto)).thenReturn(updated);

        mockMvc.perform(put("/Article/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleStatus").value("Dañado"));
    }

    @Test
    void testUpdateArticleNotFound() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "Dañado", "Desc", null);
        Mockito.when(articleService.update(99, dto)).thenThrow(new RuntimeException("Artículo no encontrado"));

        mockMvc.perform(put("/Article/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al actualizar el articulo"));
    }

    // Pruebas para delete
    @Test
    void testDeleteArticleSuccess() throws Exception {
        Article mockArticle = new Article(1, "Laptop", "Disponible", "Desc", "img.png");
        Mockito.when(articleService.delete(1)).thenReturn(mockArticle);

        mockMvc.perform(delete("/Article/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Articulo eliminado correctamente"));
    }

    @Test
    void testDeleteArticleFailure() throws Exception {
        Mockito.doThrow(new RuntimeException("Error eliminando")).when(articleService).delete(1);

        mockMvc.perform(delete("/Article/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al eliminar el articulo"));
    }

    // Pruebas para getAllAlerts
    @Test
    void testGetAllAlertsSuccess() throws Exception {
        Mockito.when(alertRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/Article/alerts"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllAlertsFailure() throws Exception {
        Mockito.when(alertRepository.findAll()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/Article/alerts"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$['Error ']").value("Error al obtener las alertas")) // Notación corregida
                .andExpect(jsonPath("$['Message ']").exists());
    }
}