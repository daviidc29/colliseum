package edu.eci.cvds.proyect.coliseum.persistency.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleService;
import org.springframework.http.MediaType;

import org.mockito.Mockito;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private AlertRepository alertRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetAllArticlesSuccess() throws Exception {
        Article article = new Article(1, "Laptop", "available", "HP", "description");
        Mockito.when(articleService.getAll()).thenReturn(List.of(article));

        mockMvc.perform(get("/Article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void testGetArticleByIdSuccess() throws Exception {
        Article article = new Article(1, "Laptop", "available", "HP", "description");
        Mockito.when(articleService.getOne(1)).thenReturn(article);

        mockMvc.perform(get("/Article/por-id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void testGetArticleByIdFailure() throws Exception {
        Mockito.when(articleService.getOne(99)).thenThrow(new RuntimeException("Not Found"));

        mockMvc.perform(get("/Article/por-id/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al obtener el articulo con ID 99"));
    }

    @Test
    void testCreateArticleSuccess() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "available", "HP", "description");
        Article saved = new Article(1, "Laptop", "available", "HP", "description");

        Mockito.when(articleService.save(Mockito.any(ArticleDto.class))).thenReturn(saved);

        mockMvc.perform(post("/Article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void testCreateArticleFailure() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "available", "HP", "description");

        Mockito.when(articleService.save(Mockito.any(ArticleDto.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/Article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al guardar el articulo"));
    }

    @Test
    void testDeleteArticleSuccess() throws Exception {
        mockMvc.perform(delete("/Article/eliminar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Articulo eliminado correctamente"));
    }

    @Test
    void testDeleteArticleFailure() throws Exception {
        Mockito.doThrow(new RuntimeException("Delete failed")).when(articleService).delete(1);

        mockMvc.perform(delete("/Article/eliminar/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al eliminar el articulo"));
    }

    @Test
    void testGetAvailableCountSuccess() throws Exception {
        Mockito.when(articleService.getAvailableCountByName("Laptop")).thenReturn(5L);

        mockMvc.perform(get("/Article/disponibles/Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibles").value(5));
    }

    @Test
    void testGetAllAlertsSuccess() throws Exception {
        Mockito.when(alertRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/Article/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void testUpdateArticleSuccess() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "available", "updated desc", "HP");
        Article updated = new Article(1, "Laptop", "available", "updated desc", "HP");

        Mockito.when(articleService.update(Mockito.eq(1), Mockito.any(ArticleDto.class))).thenReturn(updated);

        mockMvc.perform(put("/Article/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("updated desc"));
    }

    @Test
    void testUpdateArticleFailure() throws Exception {
        ArticleDto dto = new ArticleDto("Laptop", "available", "HP", "updated desc");

        Mockito.when(articleService.update(Mockito.eq(1), Mockito.any(ArticleDto.class)))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/Article/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error").value("Error al actualizar el articulo"));
    }
}

